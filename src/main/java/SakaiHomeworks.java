import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by dengshougang on 16/5/13.
 */
@Data
class Homework{
    private  String homeworkName;
    private  String stat;
    private  String date;
    private  Date date2;
    Homework(String name, String stat){
        this.homeworkName=name;
        this.stat=stat;
    }
    Homework(String name, String stat,String date){
        this.homeworkName=name;
        this.stat=stat;
        this.date=date;
        String s;
        if((s=StringUtils.remove(date,"- 迟交")).length()>=10){
            string2Date(s);
        }
    }
    private void string2Date(String in){
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        Date t;
        try {
            t = ft.parse(in);
            date2=t;
        } catch (Exception e) {
            System.out.println("Unparseable using " + ft+"  "+in);
        }
    }
    public boolean isWriteable(){
        return true;
//        if (this.stat.contains("草稿")||(stat.contains("尚未"))){
//            return true;
//        }
//        else {
//            return false;
//        }
    }
    public boolean isOvertime(){
        Date now=new Date();
        if (date2==null){
            return true;
        }
        return date2.getTime()<=now.getTime();
    }
    public boolean notOvertime(){
        return !isOvertime();
    }

    @Override
    public String toString(){
        if (this.date==null)
        return this.homeworkName+"  "+ this.stat;
        else return  String.format("%-26s%-23s%-16s",homeworkName,stat,date);
    }
}

class Usage extends JFrame{
    Usage(){
        JPanel panel=new JPanel();
        panel.setBounds(400,400,800,800);
        Dialog dialog;
        dialog=new Dialog(this,"使用说明");
        dialog.setBounds(400,400,1200,1200);
//        dialog.
        dialog.setVisible(true);

    }
}

public class SakaiHomeworks {

    @Getter  static String cookie;
    private static String[][] courseInfo;
    private static String username;
    private static String password;
    private static CloseableHttpClient httpClient;
    private static CloseableHttpResponse response=null;

//    private static  Logger logger = LoggerFactory.getLogger(SakaiHomeworks.class);
    private static final String logInUrl="http://weblogin.sustc.edu.cn/cas/login?" +
            "service=http://sakai.sustc.edu.cn/portal/login";
    private static void setDefaultHeader(HttpRequestBase httpGet){
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects", false);
        httpGet.setParams(params);
    }
    final static String mo="~~~~~~~~~~~~";
    final static String moo=mo+mo;

    public static void logIn(String username,String password){
        httpClient= HttpClients.createDefault();
        HttpGet httpGet=new HttpGet(logInUrl);
        try {
            response=httpClient.execute(httpGet);
            String data=text(response);
//            logger.debug("Firse Get Request:\n"+data);

            String s1="action=\"(.*?)\"";
            String s2="<input type=\"hidden\" name=\"lt\" .*?value=\"(.*?)\"";
            String s3="<input type=\"hidden\" name=\"execution\" .*?value=\"(.*?)\"";
            String s4="jsessionid=(.*?)type=";
            Pattern pattAction = Pattern.compile(s1);
            Pattern pattLt = Pattern.compile(s2);
            Pattern pattExec = Pattern.compile(s3);
            Pattern pattJsession=Pattern.compile(s4);
            Matcher matcherAction = pattAction.matcher(data);
            Matcher matcherLt = pattLt.matcher(data);
            Matcher matcherJsession=pattJsession.matcher(data);
            Matcher matcherExec = pattExec.matcher(data);
            if (matcherAction.find()&&matcherLt.find()&&matcherExec.find()&&matcherJsession.find()){
                String action=matcherAction.group(0);
                String lt= matcherLt.group(0);
                String execution= matcherExec.group(0);
                action=action.substring(8,action.length()-1);
                lt=lt.substring(38,lt.length()-1);
                execution=execution.substring(45,execution.length()-1);

                HttpPost httpPost = new HttpPost("http://weblogin.sustc.edu.cn"+action);
//                logger.debug(matcherJsession.group(0));
                httpPost.setHeader("Cookie",matcherJsession.group(0).substring(0,matcherJsession.group(0).length()-7));
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair("username",username));
                list.add(new BasicNameValuePair("password", new String(password)));
                list.add(new BasicNameValuePair("lt",lt));
                list.add(new BasicNameValuePair("execution",execution));
                list.add(new BasicNameValuePair("_eventId","submit"));
                list.add(new BasicNameValuePair("submit","LOGIN"));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
                httpPost.setEntity(entity);
                response=httpClient.execute(httpPost);

                setDefaultHeader(httpGet);
                response=httpClient.execute(httpGet);

                httpGet=new HttpGet(response.getFirstHeader("Location").getValue());
                setDefaultHeader(httpGet);
                response=httpClient.execute(httpGet);

                cookie=response.getLastHeader("Set-Cookie").getValue();

                httpGet=new HttpGet(response.getFirstHeader("Location").getValue());
                httpGet.setHeader("Cookie",cookie);
                setDefaultHeader(httpGet);
                response=httpClient.execute(httpGet);
//                cookie2=response.getFirstHeader("Set-Cookie").getValue();

                httpGet=new HttpGet(response.getFirstHeader("Location").getValue());
                httpGet.setHeader("Cookie",cookie);
                setDefaultHeader(httpGet);
                response=httpClient.execute(httpGet);

                courseInfo=matchCourseInfo(StringEscapeUtils.unescapeHtml4(text(response)));


            }
            else {
//                logger.error("\nError\n"+data);
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {
            if (response!=null) {
                IOUtils.closeQuietly(response);
            }
        }
    }


    public static void showAllHomeworks(){
        System.out.println("\n"+moo+"所有作业如下"+moo+"\n");
        List<Homework>[] courses=getAllCourseHomewoks(courseInfo);
        System.out.println("\n\n"+moo+"所有待完成作业如下"+moo);
        showWritableCourseHomework(courses);
    }

    private static void showHeaders(Header[] headers){
        for(int i=0;i<headers.length;i++){
            System.out.println(headers[i].getName()+" = "+headers[i].getValue());
        }
    }

    private static String getHomeworkUrl(String url){
        HttpGet httpGet=new HttpGet(url);
        httpGet.setHeader("Host","sakai.sustc.edu.cn");
        String data=null;
        String ur=null;
//        CloseableHttpClient httpClient= HttpClients.createDefault();
        try{
            response=httpClient.execute(httpGet);
            data=text(response);
            Pattern pattern=Pattern.compile("<a class=\"toolMenuLink \" href=\"(.{80,280})\" title=\"在线发布、提交和批改作业\">, \t\t\t\t\t\t\t<span class=\"toolMenuIcon icon-sakai-assignment-grades \"></span><span class=\"menuTitle\">&#20316;&#19994;</span>");
            Matcher matcher=pattern.matcher(data);
            if(!matcher.find()){
                return null;
            }
            ur=matcher.group();
            pattern=Pattern.compile("href=\"(.*?)\"");
            Matcher matcher1=pattern.matcher(ur);
            matcher1.find();
            ur=matcher1.group().substring(6,matcher1.group().length()-1);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        finally {
//            IOUtils.closeQuietly(httpClient);
            IOUtils.closeQuietly(response);
        }
        return ur;
    }

    private static void showWritableHomeworks(String url){


    }

    private static List<Homework> getHomeworks(String url){
        HttpGet httpGet=new HttpGet(url);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        configureHttpClient2(httpClientBuilder);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        httpGet.setHeader("Cookie",cookie);
        List<Homework> homeworks=new LinkedList();
        try{
            response=httpClient.execute(httpGet);
            Pattern pattern=Pattern.compile("http://sakai.sustc.edu.cn/portal/tool/(.*?)\"");
            Matcher matcher=pattern.matcher(text2(response));
            if(matcher.find()){
                String da=matcher.group();
                httpGet=new HttpGet(da.substring(0, da.length()-1));
                httpGet.setHeader("Cookie", cookie);
                response=httpClient.execute(httpGet);
                homeworks=matchHomeworks(text2(response));
            }
            else {
                System.out.println(text2(response));
                return null;
            }

        } catch (IOException e){
            e.printStackTrace();
        } catch (IllegalStateException ie){
            ie.printStackTrace();
            return null;
        }
        return homeworks;
    }

    private static List<Homework> matchHomeworks(String text){
        Pattern pattern=Pattern.compile("<a href=\"http://sakai.sustc.edu.cn/portal/tool/(.*?)</a>(.*?)<td headers=\"openDate\">(.*?)</span>");
        text=StringUtils.remove(text,"\t");
        Matcher matcher=pattern.matcher(text);
        int len=0;
        while (matcher.find()){
            len++;
        }
        String[] ss=new String[len];
        matcher=pattern.matcher(text);
        for (int i=0;matcher.find();i++){
            ss[i]=matcher.group();
        }
        return text2Homeworks(ss);
    }

    private static List<Homework> text2Homeworks(String[] ss){
        LinkedList<Homework> homeworks=new LinkedList<Homework>();
        for (int i=0;i<ss.length;i++){
            Pattern namePattern=Pattern.compile(">(.*?)</a>");
            Matcher nameMatcher=namePattern.matcher(ss[i]);
            nameMatcher.find();

            Pattern statuPattern=Pattern.compile("s\"(.*?)</td>");
            Matcher statMatcher=statuPattern.matcher(ss[i]);
            statMatcher.find();

            Pattern datePattern=Pattern.compile("ht\"(.*?)</span>");
            Matcher dateMatcher=datePattern.matcher(ss[i]);
            dateMatcher.find();

            String name=nameMatcher.group().substring(1, nameMatcher.group().length()-4);
            String stat=statMatcher.group().substring(5, statMatcher.group().length()-7);
            String date=dateMatcher.group().substring(4, dateMatcher.group().length()-7);
            homeworks.add(new Homework(name, stat, date));

        }
        return homeworks;
    }

    private static String text2(CloseableHttpResponse s) throws IOException{
        return StringEscapeUtils.unescapeHtml4(text(s));
    }

    public static void configureHttpClient(HttpClientBuilder clientBuilder){
        try
        {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
            {
                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                    return true;
                }
            }).build();

            clientBuilder.setSSLContext(sslContext);

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static List<Homework>[] getAllCourseHomewoks(String[][] courseInfo){
        List<Homework>[] courses;
        int len=courseInfo[0].length;
        courses=new LinkedList[len];
        for (int i=0;i<len;i++){
            String url=(getHomeworkUrl(StringEscapeUtils.unescapeHtml4(courseInfo[0][i])));
            if (url!=null){
                courses[i]=getHomeworks(url);
                if (courses[i]!=null){
                    for (Homework homework : courses[i]){
                        System.out.print(courseInfo[1][i]+":   ");
                        System.out.println(homework);

                    }
                }
            }
        }
        return courses;
    }

    private static void showWritableCourseHomework(List<Homework>[] courses){

        int len=courses.length;
        for (int i=0;i<len;i++){
            if (courses[i]!=null){
                for (Homework homework : courses[i]){
                    if (homework.isWriteable()
                        &&homework.notOvertime()
                        ){
                        System.out.print(courseInfo[1][i]+":   ");
                        System.out.println(homework);
                    }
                }
            }
        }
    }

        private static void showWritableCourseHomework(String[][] courseInfo){
        int len=courseInfo[0].length;
        for (int i=0;i<len;i++){
            List<Homework> homeworks;
            String url=(getHomeworkUrl(StringEscapeUtils.unescapeHtml4(courseInfo[0][i])));
            if (url!=null){
                homeworks=getHomeworks(url);
                if (homeworks!=null){
                    for (Homework homework : homeworks){
                            System.out.print(courseInfo[1][i]+":   ");
                            System.out.println(homework);
                        }
                    }
                for (Homework homework : homeworks){
                    if (homework.isWriteable()
                            &&homework.notOvertime()
                        ){
                        System.out.print(courseInfo[1][i]+":   ");
                        System.out.println(homework);
                    }
                    }
                }
            }
        }

    public static void configureHttpClient2(HttpClientBuilder clientBuilder){
        try{
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,  String authType) throws CertificateException{

                }
                public void checkServerTrusted(X509Certificate[] chain,  String authType) throws CertificateException{

                }
                public X509Certificate[] getAcceptedIssuers(){
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);

            clientBuilder.setSSLContext(ctx);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String[][] matchCourseInfo(String text){
        Matcher matcher;
        String s1="<li class=\"nav-menu\"><a href=\"(.*?)\" title=\"(.*?)\"";
        Pattern groupPattern=Pattern.compile(s1);
        matcher=groupPattern.matcher(text);
        String s2="href=\"(.*?)\"";
        String s3="title=\"(.*?)\"";
        Pattern hrefPattern=Pattern.compile(s2);
        Pattern titlePattern=Pattern.compile(s3);
        int len=0;
        for (;matcher.find();){
            len++;
        }
        Matcher[] hrefMacher=new Matcher[len];
        Matcher[] titleMacher=new Matcher[len];
        String[][] sss=new String[2][len];
        matcher=groupPattern.matcher(text);
        for (int i=0;i<len&&matcher.find();i++){
            hrefMacher[i]=hrefPattern.matcher(matcher.group());
            titleMacher[i]=titlePattern.matcher(matcher.group());
            titleMacher[i].find();
            hrefMacher[i].find();
            sss[0][i]=hrefMacher[i].group().substring(6,hrefMacher[i].group().length()-1);
            sss[1][i]=StringEscapeUtils.unescapeHtml4(titleMacher[i].group().substring(7,titleMacher[i].group().length()-1));

        }
        return sss;
    }

    private static String text(CloseableHttpResponse response)throws IOException{
        return (IOUtils.readLines(response.getEntity().getContent(),"utf-8").toString());
    }

    private static void showHeaders(CloseableHttpResponse response){
        Header[] headers=response.getAllHeaders();
        for (int i=0;i<headers.length;i++){
            System.out.println((headers[i].getName()+"="+headers[i].getValue()));
        }
    }

    private static void logInTest(){
        logIn("","");
        showAllHomeworks();
    }

    public static void main(String[] args) {

        final String softwareVersion="v0.1.2_beta";

//        logInTest();

        if (args.length==2&&args[0].length()==8&&args[0].compareTo("11110000")>0&&args[1].length()>=6) {
            username=args[0];
            password=args[1];
            System.out.println("\n"+moo+"Sakai Homeworks "+softwareVersion+" "+moo+"\n");
            logIn(username,password);
            showAllHomeworks();
        }
        else {
            System.out.println("请输入你的学号和密码,我们绝不会以任何方式记录你的密码!");
            System.out.println("一个合法的输入示例为: java -jar C:\\\\*******.jar 11310888 qwer1234");
        }
    }

}
