<?php
define('DBHOST', 'localhost');
define('DBNAME', 'cedars_voice');
define('DBUSER', 'root');
define('DBPASS', '');
define('DBCONNSTRING', 'mysql:host='.DBHOST.';dbname='.DBNAME);

// Function to establish a database connection
function connectDB() {
    try {
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $pdo;
    } catch (PDOException $e) {
        die("Connection failed: " . $e->getMessage());
    }
}

// Function to record a vote in the database
function recordVote($voter_id, $candidate_id) {
    $pdo = connectDB();
    try {
        // Prepare SQL statement
        $stmt = $pdo->prepare("INSERT INTO votes (voter_id, candidate_id) VALUES (:voter_id, :candidate_id)");
        
        // Bind parameters
        $stmt->bindParam(':voter_id', $voter_id);
        $stmt->bindParam(':candidate_id', $candidate_id);
        
        // Execute the statement
        $stmt->execute();
        
        // Return success message
        echo "Vote recorded successfully.";
    } catch (PDOException $e) {
        // Handle errors
        echo "Error: " . $e->getMessage();
    }
}

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Check if voter_id and candidate_id are set in the request
    if (isset($_POST['voter_id']) && isset($_POST['candidate_id'])) {
        // Get voter_id and candidate_id from the request
        $voter_id = $_POST['voter_id'];
        $candidate_id = $_POST['candidate_id'];
        
        // Call function to record the vote
        recordVote($voter_id, $candidate_id);
    } else {
        // Handle missing parameters
        echo "Error: Missing parameters.";
    }
} else {
    // Handle invalid request method
    echo "Error: Invalid request method.";
}
?>
