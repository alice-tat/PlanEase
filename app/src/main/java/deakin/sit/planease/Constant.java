package deakin.sit.planease;

import deakin.sit.planease.dto.Goal;

public class Constant {
    public static final String BACKEND_URL = "http://192.168.4.28:5000";

    public static final String USER_REGISTER_ROUTE = "/user/register";
    public static final String USER_LOGIN_ROUTE = "/user/login";
    public static final String USER_DELETE_ROUTE = "/user/delete";


    public static final String TASK_CRUD_ROUTE = "/task";
    public static final String GOAL_CRUD_ROUTE = "/goal";

    public static final String SAVE_GENERATED_TASK_ROUTE = "/ai/saveGeneratedTask";
    public static final String AI_ASSISTANT_ROUTE = "/ai/getTaskSuggestion";
}
