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

        // Prepare a SQL query to check if the voter has voted in the voters table
        $stmt = $conn->prepare("SELECT has_voted FROM voters WHERE voter_id = ?");
        $stmt->execute([$voter_id]);

        // Get the result
        $result = $stmt->fetch();

        // Check if the voter has voted
        if ($result['has_voted'] == 1) {
            // The voter has voted
            echo json_encode(array("hasVoted" => true));
        } else {
            // The voter has not voted
            echo json_encode(array("hasVoted" => false));
        }
    } catch(PDOException $e) {
        echo "Connection failed: " . $e->getMessage();
    }
?>