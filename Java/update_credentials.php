<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "xxx";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$old_username = $_POST['old_username']; // Fetch old username
$old_password = $_POST['old_password'];
$new_password = $_POST['new_password'];

// Fetch the current password hash from the database
$sql = "SELECT password FROM users WHERE username = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $old_username);
$stmt->execute();
$stmt->store_result();
$stmt->bind_result($current_hashed_password);
$stmt->fetch();

// Check if the old password matches
if (password_verify($old_password, $current_hashed_password)) {
    // Hash the new password
    $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);

    // Update the password
    $sql_update = "UPDATE users SET password = ? WHERE username = ?";
    $stmt_update = $conn->prepare($sql_update);
    $stmt_update->bind_param("ss", $hashed_password, $old_username);

    if ($stmt_update->execute()) {
        echo "success";
    } else {
        echo "error: " . $stmt_update->error; // Output MySQL error for debugging
    }

    $stmt_update->close();
} else {
    echo "error: Old password is incorrect"; // Error message if old password does not match
}

$stmt->close();
$conn->close();
?>
