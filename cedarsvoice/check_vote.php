<?php
    // Include the config.php file
    require_once 'config.php';

    // Get the voter ID from the request
    $voter_id = $_POST['id'];

    try {
        // Create connection
        $conn = new PDO(DBCONNSTRING, DBUSER, DBPASS);

        // Set the PDO error mode to exception
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Prepare a SQL query to check if the voter ID exists in the votes table
        $stmt = $conn->prepare("SELECT * FROM votes WHERE voter_id = ?");
        $stmt->execute([$voter_id]);

        // Get the result
        $result = $stmt->fetchAll();

        // Check if the voter ID exists in the votes table
        if (count($result) > 0) {
            // The voter ID exists in the votes table
            echo json_encode(array("hasVoted" => true));
        } else {
            // The voter ID does not exist in the votes table
            echo json_encode(array("hasVoted" => false));
        }
    } catch(PDOException $e) {
        echo "Connection failed: " . $e->getMessage();
    }
?>