package deakin.sit.planease;


public class Constant {
    // Backend URL
    public static final String BACKEND_URL = "http://192.168.4.28:5000";

    // User routes
    public static final String USER_REGISTER_ROUTE = "/user/register";
    public static final String USER_LOGIN_ROUTE = "/user/login";
    public static final String USER_DELETE_ROUTE = "/user/delete";

    // Task routes
    public static final String TASK_CRUD_ROUTE = "/task";
    public static final String TASK_FINISH_ROUTE = "/task/finish";

    // Goal routes
    public static final String GOAL_CRUD_ROUTE = "/goal";
    public static final String GOAL_FINISH_ROUTE = "/goal/finish";

    // AI routes
    public static final String SAVE_GENERATED_TASK_ROUTE = "/ai/saveGeneratedTask";
    public static final String AI_ASSISTANT_ROUTE = "/ai/getTaskSuggestion";
}
