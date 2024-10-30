package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class NutritionQuizApp {
    private static final Map<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        loadUsersFromFile();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Welcome to the Nutrition Quiz!");
            System.out.println("1. Register");
            System.out.println("2. Log in");
            System.out.println("3. Guest login");
            System.out.println("4. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser(scanner);
                    break;
                case 2:
                    loginUser(scanner);
                    break;
                case 3:
                    guestLogin();
                    break;
                case 4:
                    saveUsersToFile();
                    System.out.println("Exiting the program. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void registerUser(Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();

        // Validation
        if (password.contains(name)) {
            System.out.println("Password must not contain the user's name.");
            return;
        }

        if (!email.contains("@")) {
            System.out.println("Email must contain '@'.");
            return;
        }

        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d{10}")) {
            System.out.println("Phone number must contain exactly 10 digits.");
            return;
        }

        if (users.containsKey(email)) {
            System.out.println("User already registered with this email.");
            return;
        }

        User user = new User(name, password, email, phoneNumber);
        users.put(email, user);
        System.out.println("Registration successful!");
    }

    private static void loginUser(Scanner scanner) {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = users.get(email);
        if (user != null && user.password().equals(password)) {
            System.out.println("Login successful! Welcome " + user.name());
            takeQuiz(scanner);
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    private static void guestLogin() {
        System.out.println("Guest login successful! You can take the quiz as a guest.");
        takeQuiz(new Scanner(System.in));
    }

    private static void takeQuiz(Scanner scanner) {
        List<String[]> allQuestions = loadQuizFromFile();
        List<String[]> currentQuestions = new ArrayList<>(allQuestions);

        boolean wonDiscount = false;

        while (true) {
            if (currentQuestions.size() < 5) {
                System.out.println("Not enough questions to start a new quiz.");
                return;
            }

            Collections.shuffle(currentQuestions);
            List<String[]> quizQuestions = currentQuestions.subList(0, 5);
            boolean correct = conductQuiz(quizQuestions, scanner);

            if (correct && !wonDiscount) {
                displayImage("trophy.png");
                System.out.println("Congratulations! You answered all questions correctly!");
                System.out.println("You win a 30% discount for online shopping at NutritionJavaProject.com!");
                wonDiscount = true;
            } else if (!correct) {
                displayImage("sad_face.png");
                System.out.println("You did not answer all questions correctly. Try again!");
            }

            System.out.print("Do you want to play again? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(response)) {
                System.out.println("\033[32mOK, let's give it another try!\033[0m");
                currentQuestions.removeAll(quizQuestions);
            } else {
                displayThankYouMessage();
                System.exit(0);
            }
        }
    }
    private static void displayThankYouMessage() {
        System.out.println("\033[1;34mThank you for playing!\033[0m");
        System.out.println("\033[31mAnd don't forget.. an apple a day keeps the doctor away!\033[0m");
    }
    private static boolean conductQuiz(List<String[]> questions, Scanner scanner) {
        int correctAnswers = 0;
        for (String[] qa : questions) {
            System.out.println("Question: " + qa[0]);
            System.out.print("Your answer: ");
            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase(qa[1])) {
                correctAnswers++;
            }
        }

        return correctAnswers == 5;
    }

    private static List<String[]> loadQuizFromFile() {
        List<String[]> questions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(NutritionQuizApp.class.getClassLoader().getResourceAsStream("quiz.txt"))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    questions.add(parts);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("quiz.txt not found in resources folder.");
        } catch (IOException e) {
            System.out.println("Error reading quiz data: " + e.getMessage());
        }
        return questions;
    }

    private static void displayImage(String imagePath) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(NutritionQuizApp.class.getClassLoader().getResource(imagePath)));
        JLabel label = new JLabel(icon);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(300, 300);
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(NutritionQuizApp.class.getClassLoader().getResourceAsStream("users.txt"))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    User user = User.fromString(line);
                    users.put(user.email(), user);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error parsing user data: " + e.getMessage() + " - Line: " + line);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("users.txt not found in resources folder.");
        } catch (IOException e) {
            System.out.println("Error reading user data: " + e.getMessage());
        }
    }

    private static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/users.txt"))) {
            for (User user : users.values()) {
                writer.write(user.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }
}

record User(String name, String password, String email, String phoneNumber) {

    @Override
    public String toString() {
        return String.join("|", name, password, email, phoneNumber);
    }

    public static User fromString(String userString) {
        String[] parts = userString.split("\\|");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid user data format.");
        }
        return new User(parts[0], parts[1], parts[2], parts[3]);
    }
}

