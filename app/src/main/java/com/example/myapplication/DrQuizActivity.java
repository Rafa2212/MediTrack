package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that presents a quiz to determine the user's body type.
 * The quiz consists of multiple questions with three possible answers each.
 * Based on the user's answers, the activity calculates and saves the user's body type.
 */
public class DrQuizActivity extends AppCompatActivity {

    private TextView textViewQuestionNumber;
    private TextView textViewQuestion;
    private RadioGroup radioGroupAnswers;
    private RadioButton radioButtonA;
    private RadioButton radioButtonB;
    private RadioButton radioButtonC;
    private MaterialButton buttonPrevious;
    private MaterialButton buttonNext;
    private MaterialButton buttonSubmitQuiz;

    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private final Map<Integer, String> userAnswers = new HashMap<>();

    /**
     * Initializes the activity, sets up UI components, and configures event listeners.
     * This method is called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_type_quiz);

        textViewQuestionNumber = findViewById(R.id.textViewQuestionNumber);
        textViewQuestion = findViewById(R.id.textViewQuestion);
        radioGroupAnswers = findViewById(R.id.radioGroupAnswers);
        radioButtonA = findViewById(R.id.radioButtonA);
        radioButtonB = findViewById(R.id.radioButtonB);
        radioButtonC = findViewById(R.id.radioButtonC);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        buttonSubmitQuiz = findViewById(R.id.buttonSubmitQuiz);

        initializeQuestions();

        displayQuestion(currentQuestionIndex);

        buttonPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion(currentQuestionIndex);
                updateNavigationButtons();
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (radioGroupAnswers.getCheckedRadioButtonId() == -1) {
                Snackbar.make(findViewById(android.R.id.content), "Please select an answer", Snackbar.LENGTH_SHORT).show();
                return;
            }

            saveCurrentAnswer();
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
                updateNavigationButtons();
            }
        });

        buttonSubmitQuiz.setOnClickListener(v -> {
            if (radioGroupAnswers.getCheckedRadioButtonId() == -1) {
                Snackbar.make(findViewById(android.R.id.content), "Please select an answer", Snackbar.LENGTH_SHORT).show();
                return;
            }

            saveCurrentAnswer();
            String bodyType = determineBodyType();
            saveBodyType(bodyType);

            Snackbar.make(findViewById(android.R.id.content),
                    "Your body type is: " + bodyType, Snackbar.LENGTH_LONG).show();

            findViewById(android.R.id.content).postDelayed(() -> {
                Intent intent = new Intent();
                intent.putExtra("bodyType", bodyType);
                setResult(RESULT_OK, intent);
                finish();
            }, 2000);
        });
    }

    /**
     * Initializes the list of quiz questions.
     * Creates a set of predefined questions about body characteristics
     * that will be used to determine the user's body type.
     */
    private void initializeQuestions() {
        questions = new ArrayList<>();

        questions.add(new QuizQuestion(
                "From an objective point of view, which of the following factors seems most prominent (or dominant) on your body when you look in the mirror?",
                "A. Bone",
                "B. Muscle",
                "C. Body fat"
        ));

        questions.add(new QuizQuestion(
                "How do your shoulders compare to your hips?",
                "A. My shoulders are narrower than my hips.",
                "B. They're approximately the same width as my hips.",
                "C. My shoulders are wider than my hips."
        ));

        questions.add(new QuizQuestion(
                "Which of the following objects best describes your body shape?",
                "A. A pencil",
                "B. An hourglass",
                "C. A pear"
        ));

        questions.add(new QuizQuestion(
                "If you encircle one wrist with your other hand's middle finger and thumb, what happens?",
                "A. My middle finger and thumb overlap a bit.",
                "B. My middle finger and thumb touch, but just barely.",
                "C. There's a gap between my middle finger and thumb."
        ));

        questions.add(new QuizQuestion(
                "When it comes to your weight, which of the following patterns best describes your history?",
                "A. I have trouble gaining muscle or body fat.",
                "B. I can gain and lose weight without too much difficulty.",
                "C. I gain weight easily but have a hard time losing it."
        ));

        questions.add(new QuizQuestion(
                "Think about what your body looked like, before you corrupted it with poor dietary and exercise habits, once you reached your full height as a teenager or young adult. How did you look?",
                "A. I looked long and lanky.",
                "B. I looked strong and compact.",
                "C. I looked soft and full bodied."
        ));

        questions.add(new QuizQuestion(
                "If you'd been exercising regularly and you were to take a break for a few months, what would happen to your body?",
                "A. I would lose muscle and strength quickly.",
                "B. My body wouldn't change that much.",
                "C. My body would soften up significantly and I might even gain weight."
        ));

        questions.add(new QuizQuestion(
                "Put on a pair of form-fitting jeans — where on your body do they get extra clingy or even stuck?",
                "A. They don't. In fact, I can't keep them up without a belt.",
                "B. With a bit of work, I can wriggle my way into them over my muscular thighs.",
                "C. They get caught on my butt or belly."
        ));

        questions.add(new QuizQuestion(
                "When you have a serious carb-fest (think: heaping plate of pasta or multiple slices of pizza), how do you feel afterward?",
                "A. The same as I usually do — normal, really.",
                "B. I generally feel good, though I notice my ab muscles are extra hard or my belly feels full.",
                "C. More often than not, I feel tired or bloated for a few hours after the meal."
        ));

        questions.add(new QuizQuestion(
                "How would you describe your body's bone structure?",
                "A. I have a small frame.",
                "B. I have a medium frame.",
                "C. I have a relatively large frame."
        ));
    }

    /**
     * Displays a specific question and its answer options on the screen.
     * Also restores any previously selected answer for this question.
     *
     * @param index The index of the question to display
     */
    @SuppressLint("SetTextI18n")
    private void displayQuestion(int index) {
        QuizQuestion question = questions.get(index);
        textViewQuestionNumber.setText("Question " + (index + 1) + "/" + questions.size());
        textViewQuestion.setText(question.getQuestion());
        radioButtonA.setText(question.getOptionA());
        radioButtonB.setText(question.getOptionB());
        radioButtonC.setText(question.getOptionC());

        radioGroupAnswers.clearCheck();

        if (userAnswers.containsKey(index)) {
            String answer = userAnswers.get(index);
            if ("A".equals(answer)) {
                radioButtonA.setChecked(true);
            } else if ("B".equals(answer)) {
                radioButtonB.setChecked(true);
            } else if ("C".equals(answer)) {
                radioButtonC.setChecked(true);
            }
        }
    }

    /**
     * Saves the user's selected answer for the current question.
     * The answer is stored in the userAnswers map with the question index as the key.
     */
    private void saveCurrentAnswer() {
        int selectedId = radioGroupAnswers.getCheckedRadioButtonId();
        if (selectedId != -1) {
            if (selectedId == R.id.radioButtonA) {
                userAnswers.put(currentQuestionIndex, "A");
            } else if (selectedId == R.id.radioButtonB) {
                userAnswers.put(currentQuestionIndex, "B");
            } else if (selectedId == R.id.radioButtonC) {
                userAnswers.put(currentQuestionIndex, "C");
            }
        }
    }

    /**
     * Updates the navigation buttons based on the current question index.
     * - Enables/disables the previous button based on whether there are previous questions
     * - Shows/hides the next and submit buttons based on whether this is the last question
     * - Adjusts the layout parameters of the buttons accordingly
     */
    private void updateNavigationButtons() {
        buttonPrevious.setEnabled(currentQuestionIndex > 0);

        if (currentQuestionIndex == questions.size() - 1) {
            buttonNext.setVisibility(View.GONE);
            buttonSubmitQuiz.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            buttonPrevious.setLayoutParams(params);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
            buttonSubmitQuiz.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            params.setMarginEnd(8);
            buttonPrevious.setLayoutParams(params);
        }
    }

    /**
     * Determines the user's body type based on their quiz answers.
     * Counts the number of A, B, and C answers and calculates the predominant body type.
     * 
     * @return A string representing the user's body type (Ectomorph, Mesomorph, Endomorph, 
     *         or a combination of these types)
     */
    private String determineBodyType() {
        int countA = 0;
        int countB = 0;
        int countC = 0;

        for (String answer : userAnswers.values()) {
            if ("A".equals(answer)) {
                countA++;
            } else if ("B".equals(answer)) {
                countB++;
            } else if ("C".equals(answer)) {
                countC++;
            }
        }

        String primaryType;
        if (countA > countB && countA > countC) {
            primaryType = "Ectomorph";
        } else if (countB > countA && countB > countC) {
            primaryType = "Mesomorph";
        } else if (countC > countA && countC > countB) {
            primaryType = "Endomorph";
        } else if (countA == countB && countA > countC) {
            primaryType = "Ecto-Mesomorph";
        } else if (countB == countC && countB > countA) {
            primaryType = "Meso-Endomorph";
        } else if (countA > countB) {
            primaryType = "Ecto-Endomorph";
        } else {
            primaryType = "Balanced";
        }

        return primaryType;
    }

    /**
     * Saves the determined body type to the user's profile in the database.
     * Retrieves the user ID from shared preferences, gets the user's profile,
     * updates the body type, and saves the updated profile.
     *
     * @param bodyType The determined body type to save
     */
    private void saveBodyType(String bodyType) {
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        if (!userId.isEmpty()) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            UserProfile userProfile = dbHelper.getUserProfile(userId);

            if (userProfile != null) {
                userProfile.setBodyType(bodyType);
                dbHelper.insertOrUpdateProfile(userId, userProfile);
            }
        }
    }

    /**
     * Inner class representing a quiz question with three possible answers.
     * Each question has a question text and three answer options (A, B, and C).
     */
    private static class QuizQuestion {
        private final String question;
        private final String optionA;
        private final String optionB;
        private final String optionC;

        /**
         * Constructs a new QuizQuestion with the specified question text and answer options.
         *
         * @param question The text of the question
         * @param optionA The text for option A
         * @param optionB The text for option B
         * @param optionC The text for option C
         */
        public QuizQuestion(String question, String optionA, String optionB, String optionC) {
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
        }

        /**
         * Gets the question text.
         *
         * @return The question text
         */
        public String getQuestion() {
            return question;
        }

        /**
         * Gets the text for option A.
         *
         * @return The text for option A
         */
        public String getOptionA() {
            return optionA;
        }

        /**
         * Gets the text for option B.
         *
         * @return The text for option B
         */
        public String getOptionB() {
            return optionB;
        }

        /**
         * Gets the text for option C.
         *
         * @return The text for option C
         */
        public String getOptionC() {
            return optionC;
        }
    }
}
