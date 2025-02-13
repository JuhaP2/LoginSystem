<?php
// Database connection credentials
$servername = "localhost";
$username = "root"; 
$password = ""; 
$database = "xxx";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Handle requests from Android app
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['action']) && $_POST['action'] === 'login') {
        $username = $_POST['username'];
        $password = $_POST['password'];

        // Perform login query with prepared statement to prevent SQL injection
        $stmt = $conn->prepare("SELECT password, usertype FROM users WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $hashed_password = $row['password'];
            $usertype = $row['usertype'];

            if (password_verify($password, $hashed_password)) {
                // Password is correct
                echo json_encode([
                    "status" => "success",
                    "usertype" => $usertype // Sending usertype
                ]);
            } else {
                // Wrong password
                echo json_encode([
                    "status" => "failed",
                    "message" => "Login failed"
                ]);
            }
        } else {
            // User not found
            echo json_encode([
                "status" => "failed",
                "message" => "Login failed"
            ]);
        }
        $stmt->close();
    }
}

$conn->close();
?>