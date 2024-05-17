<?php
// Include the database connection file
require_once('config.php');

// Check if voter ID is provided in the request
if(isset($_GET['national_id'])) {
    // Sanitize the input to prevent SQL injection
    $voter_id = $_GET['national_id'];
    
    try {
        // Establish database connection
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        
        // Prepare SQL statement to select voter with given ID
        $stmt = $pdo->prepare("SELECT * FROM voters WHERE voter_id = :voter_id");
        $stmt->bindParam(':voter_id', $voter_id);
        $stmt->execute();
        
        // Check if voter exists
        if ($stmt->rowCount() > 0) {
            // Voter found, send success response
            echo "exists";
        } else {
            // Voter not found, send error response
            echo "does not exist";
        }
        
        // Close database connection
        $pdo = null;
    } catch (PDOException $e) {
        // Handle database connection error
        die($e->getMessage());
    }
} else {
    // Voter ID not provided, send error response
    echo "Voter ID is required";
}
?>
