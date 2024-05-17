<?php
// Include the config file
require_once 'config.php';

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Get the voter ID from the request
    $voter_id = $_GET['voter_id'];

    // Prepare SQL statement
    $sql = "SELECT voter_fingerprint, fingerprint_iv FROM voters WHERE voter_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$voter_id]);

    // Check if a row is returned
    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $fingerprint = $row["voter_fingerprint"];
        $fingerprint_iv = $row["fingerprint_iv"];

        // Concatenate the fingerprint and IV with a separator (e.g., ':')
        $response = $fingerprint . ":" . $fingerprint_iv;
        echo $response;
    } else {
        echo "Fingerprint not found";
    }
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}

// Close connection
$pdo = null;
?>