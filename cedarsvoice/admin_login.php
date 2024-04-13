<?php
require_once('config.php');

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Get admin name and password from POST request
    $adminName = $_POST['admin_name'];
    $adminPassword = $_POST['admin_password'];

    // Prepare SQL statement to select admin with given name and password
    $stmt = $pdo->prepare("SELECT * FROM Admins WHERE admin_name = :admin_name AND admin_password = :admin_password");
    $stmt->bindParam(':admin_name', $adminName);
    $stmt->bindParam(':admin_password', $adminPassword);
    $stmt->execute();

    // Check if admin exists
    if ($stmt->rowCount() > 0) {
        // Admin exists, send success response
        echo "success";
    } else {
        // Admin not found, send error response
        echo "error";
    }

    // Close database connection
    $pdo = null;
} catch (PDOException $e) {
    // Handle database connection error
    die($e->getMessage());
}
?>
