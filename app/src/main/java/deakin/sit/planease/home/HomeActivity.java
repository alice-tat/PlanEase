package deakin.sit.planease.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import deakin.sit.planease.GoalFormActivity;
import deakin.sit.planease.LoginActivity;
import deakin.sit.planease.R;
import deakin.sit.planease.TaskFormActivity;
import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.dto.User;

public class HomeActivity extends AppCompatActivity {
    // Views
    FragmentContainerView fragmentContainerView;
    BottomNavigationView bottomNavigationView;

    // Others
    FragmentManager fragmentManager;

    // User data
    User currentUser;

//    ActivityResultLauncher<Intent> activityResultLauncher;
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

        // Get ids
        int idMenuNavigationTaskList = R.id.navigationTaskList;
        int idMenuNavigationGoalList = R.id.navigationGoalList;
        int idMenuNavigationAIChat = R.id.navigationAIChat;
        int idMenuNavigationAccount = R.id.navigationAccount;

        // Setup views
        fragmentContainerView = findViewById(R.id.fragmentContainerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Config fragments
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, new TaskListFragment()).commit();

        // Config navigation bar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == idMenuNavigationTaskList) {
                setCurrentFragment(new TaskListFragment());
                return true;
            } else if (itemId == idMenuNavigationGoalList) {
                setCurrentFragment(new GoalListFragment());
                return true;
            } else if (itemId == idMenuNavigationAIChat) {
                setCurrentFragment(new AIChatFragment());
                return true;
            } else if (itemId == idMenuNavigationAccount) {
                setCurrentFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        // Config register launcher
//        activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                this::handleResultFromActivity
//        );
    }

    public void setCurrentFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .commit();
    }

//    public void startFormActivity(Task task) {
//        Intent intent = new Intent(HomeActivity.this, TaskFormActivity.class);
//        intent.putExtra("User", currentUser);
//        intent.putExtra("Task", task);
//        startActivity(intent);
//    }
//
//    public void startFormActivity(Goal goal) {
//        Intent intent = new Intent(HomeActivity.this, GoalFormActivity.class);
//        intent.putExtra("User", currentUser);
//        intent.putExtra("Goal", goal);
//        startActivity(intent);
//    }
//
//    private void handleResultFromActivity(ActivityResult result) {
//        String returnedMessage = "";
//        if (result.getResultCode() == RESULT_OK) {
//            returnedMessage = result.getData()!=null ? result.getData().getStringExtra("Message") : "Result OK";
//        } else {
//            returnedMessage = result.getData()!=null ? result.getData().getStringExtra("Message") : "Result Cancelled";
//        }
//        Toast.makeText(this, returnedMessage, Toast.LENGTH_SHORT).show();
//    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}