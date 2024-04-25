<?php
// Include the database connection file
require_once('config.php');

// Check if supervisor ID is provided in the request
if(isset($_GET['supervisor_id'])) {
    // Sanitize the input to prevent SQL injection
    $supervisor_id = $_GET['supervisor_id'];
    
    try {
        // Establish database connection
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        
        // Prepare SQL statement to select supervisor with given ID
        $stmt = $pdo->prepare("SELECT * FROM supervisors WHERE supervisor_id = :supervisor_id");
        $stmt->bindParam(':supervisor_id', $supervisor_id);
        $stmt->execute();
        
        // Check if supervisor exists
        if ($stmt->rowCount() > 0) {
            // Supervisor found, send success response
            echo "exists";
        } else {
            // Supervisor not found, send error response
            echo "does not exist";
        }
        
        // Close database connection
        $pdo = null;
    } catch (PDOException $e) {
        // Handle database connection error
        die($e->getMessage());
    }
} else {
    // Supervisor ID not provided, send error response
    echo "Supervisor ID is required";
}
?>