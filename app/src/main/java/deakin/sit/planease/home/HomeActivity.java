package deakin.sit.planease.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import deakin.sit.planease.R;
import deakin.sit.planease.dto.Message;
import deakin.sit.planease.dto.User;

public class HomeActivity extends AppCompatActivity {
    public static final int ID_MENU_NATIVATION_TASK_LIST = R.id.navigationTaskList;
    public static final int ID_MENU_NATIVATION_GOAL_LIST = R.id.navigationGoalList;
    public static final int ID_MENU_NATIVATION_AI_CHAT = R.id.navigationAIChat;
    public static final int ID_MENU_NATIVATION_ACCOUNT = R.id.navigationAccount;

    FragmentManager fragmentManager;
    FragmentContainerView fragmentContainerView;
    BottomNavigationView bottomNavigationView;

    User currentUser;
    List<Message> currentAINessageList;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Get intent data
        currentUser = (User) getIntent().getSerializableExtra("User");

        currentAINessageList = new ArrayList<Message>();
        currentAINessageList.add(new Message(
                "Welcome user", "AI",
                true, "", "")
        ); // Message(content, username, isAIgenerated, provided goal name, provided goal date)

        // Setup views
        fragmentContainerView = findViewById(R.id.fragmentContainerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Config fragments
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, new TaskListFragment()).commit();

        // Config navigation bar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == ID_MENU_NATIVATION_TASK_LIST) {
                setCurrentFragment(new TaskListFragment());
                return true;
            } else if (itemId == ID_MENU_NATIVATION_GOAL_LIST) {
                setCurrentFragment(new GoalListFragment());
                return true;
            } else if (itemId == ID_MENU_NATIVATION_AI_CHAT) {
                setCurrentFragment(new AIChatFragment());
                return true;
            } else if (itemId == ID_MENU_NATIVATION_ACCOUNT) {
                setCurrentFragment(new AccountFragment());
                return true;
            }
            return false;
        });
    }

    public void setCurrentFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .commit();
    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}