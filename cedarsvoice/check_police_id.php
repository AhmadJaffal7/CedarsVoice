<?php
// Include the database connection file
require_once('config.php');

// Check if police ID is provided in the request
if(isset($_GET['police_id'])) {
    // Sanitize the input to prevent SQL injection
    $police_id = $_GET['police_id'];
    
    try {
        // Establish database connection
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        
        // Prepare SQL statement to select police with given ID
        $stmt = $pdo->prepare("SELECT * FROM police WHERE police_id = :police_id");
        $stmt->bindParam(':police_id', $police_id);
        $stmt->execute();
        
        // Check if police exists
        if ($stmt->rowCount() > 0) {
            // Police found, send success response
            echo "exists";
        } else {
            // Police not found, send error response
            echo "does not exist";
        }
        
        // Close database connection
        $pdo = null;
    } catch (PDOException $e) {
        // Handle database connection error
        die($e->getMessage());
    }
} else {
    // Police ID not provided, send error response
    echo "Police ID is required";
}
?>