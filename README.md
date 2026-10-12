# Online Exam System

A desktop quiz and exam application built with **Java Swing** and **JDBC**, backed by a **MySQL** database. Admins create exams and questions; students log in, take exams, and see their results.

## Features

- Login screen with role-based access (admin / student)
- Dashboard for navigating the app
- Exam screen for attempting questions
- Admin: add and manage exams and questions
- Student: take an exam and view results
- Results stored in the database and viewable later

## Tech Stack

| Layer      | Technology                 |
|------------|----------------------------|
| Language   | Java (JDK 17 or newer)     |
| GUI        | Java Swing                 |
| Database   | MySQL                      |
| Connectivity | JDBC (MySQL Connector/J) |
| Design     | DAO pattern, model classes |

## Project Structure

```
OnlineExam/
├── lib/
│   └── mysql-connector-j-26.7.0.jar   # JDBC driver
├── sql/
│   └── schema.sql                     # database + tables
├── src/
│   ├── app/       Main.java           # entry point
│   ├── dao/       ExamDAO, QuestionDAO, ResultDAO, UserDAO
│   ├── model/     Exam, Question, Result, User
│   ├── ui/        LoginFrame, DashboardFrame, ExamFrame
│   └── util/      DBConnection.java   # DB connection helper
└── README.md
```

## Prerequisites

- JDK 17 or newer (`java -version` and `javac -version` should both work)
- MySQL Server running locally
- VS Code with the **Extension Pack for Java** (or any Java IDE)

## Setup

### 1. Create the database

Open MySQL and run the schema:

```bash
mysql -u root -p < sql/schema.sql
```

Or open `sql/schema.sql` in a MySQL client and execute it.

### 2. Configure the database connection

Open `src/util/DBConnection.java` and set your own MySQL details:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/online_exam";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

> Never commit your real password. Keep it as a placeholder in the repo.

### 3. Add the JDBC driver

The driver is already in `lib/`. In VS Code, open **Java Projects → Referenced Libraries** and make sure `mysql-connector-j-26.7.0.jar` is listed.

### 4. Run the app

- **VS Code:** open `src/app/Main.java` and click **Run** above `main()`.
- **Terminal:**

```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" app.Main
```

(On Windows, use `;` instead of `:` in the classpath.)

## How to Use

1. Launch the app and log in.
2. **Admin:** add exams and questions from the dashboard.
3. **Student:** pick an exam, answer the questions, and submit.
4. View your score on the results screen.

## Screenshots

_Add screenshots of the login, dashboard, exam, and results screens here._

## Concepts Demonstrated

- Swing GUI with multiple frames
- JDBC with prepared statements
- DAO pattern separating database logic from the UI
- Packages and model classes
- Exception handling for database and input errors

## Future Improvements

- Timer for exams
- Password hashing
- Question randomization
- Export results to CSV or PDF

## Author

Om Aditya
