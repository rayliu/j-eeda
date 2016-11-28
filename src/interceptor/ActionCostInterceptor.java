package interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.log.Logger;

public class ActionCostInterceptor implements Interceptor {
    private Logger logger = Logger.getLogger(ActionCostInterceptor.class);
  
    @Override
    public void intercept(Invocation ai) {
        long start = System.currentTimeMillis();
        ai.invoke();
        long end = System.currentTimeMillis();
        long renderTime = end - start;
        logger.debug(ai.getControllerKey()+"."+ai.getMethodName()+" action cost:"+renderTime+"ms");
    }

}
