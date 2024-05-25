<?php
// Include the config file
require_once 'config.php';

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Query to fetch candidate information including last name
    $sql = "SELECT candidate_name, candidate_last_name, vote_count FROM candidates";
    $stmt = $pdo->query($sql);

    if ($stmt) {
        $response = ""; // Initialize response string

        // Fetch and append each candidate's information to the response string
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $candidateName = $row["candidate_name"];
            $candidateLastName = $row["candidate_last_name"];
            $voteCount = $row["vote_count"];
            $response .= $candidateName . " " . $candidateLastName . ":" . $voteCount . ","; // Concatenate name, last name, and vote count with delimiter
        }

        // Remove the trailing comma from the response
        $response = rtrim($response, ",");

        // Output the response
        echo $response;
    } else {
        echo "No candidates found";
    }
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}

// Close connection
$pdo = null;
?>
