import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created by dengshougang on 16/5/13.
 */
public class TestLogIn {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(SakaiHomeworks.class);
        for (Failure failure:result.getFailures()){
            System.out.println(failure);
        }
    }
}
