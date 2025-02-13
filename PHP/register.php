<?php
// Database connection credentials
$servername = "localhost";
$username = "root"; // Change this
$password = ""; // Change this
$database = "xxx";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Handle requests from Android app
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['action']) && $_POST['action'] === 'register') {
        $username = $_POST['username'];
        $password = $_POST['password'];
        $usertype = $_POST['usertype']; 

        $stmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            echo json_encode([
                "status" => "failed",
                "message" => "Username already exists"
            ]);
        } else {
            // Hash the password before saving
            $hashed_password = password_hash($password, PASSWORD_DEFAULT);

            // Insert the new user into the database
            $stmt = $conn->prepare("INSERT INTO users (username, password, usertype) VALUES (?, ?, ?)");
            $stmt->bind_param("sss", $username, $hashed_password, $usertype);

            if ($stmt->execute()) {
                // Registration successful
                echo json_encode([
                    "status" => "success",
                    "message" => "Registration successful"
                ]);
            } else {
                // Registration failed
                echo json_encode([
                    "status" => "failed",
                    "message" => "Registration failed"
                ]);
            }
        }
        $stmt->close();
    }
}

$conn->close();
?>