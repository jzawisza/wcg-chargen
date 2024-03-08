package com.wcg.chargen.backend.testUtil;

public class PostConstructUtil
{
    private static final String POST_CONSTRUCT_METHOD_NAME = "postConstruct";

    /**
     * Helper method to invoke postConstruct method defined in services
     * from unit tests.
     *
     * This just rethrows any exceptions it raises because some of the unit tests
     * need the exception to be thrown to test negative scenarios.
     *
     * @param objClass Class with a defined postConstruct method
     * @param obj Instantiated instance of the class
     * @throws Exception
     */
    public static void invokeMethod(Class objClass, Object obj) throws Exception {
        var postConstructMethod = objClass.getDeclaredMethod(POST_CONSTRUCT_METHOD_NAME);
        postConstructMethod.setAccessible(true);
        postConstructMethod.invoke(obj);
    }
}
