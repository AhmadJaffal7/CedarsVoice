<?php
define('DBHOST', 'localhost');
define('DBNAME', 'cedars_voice');
define('DBUSER', 'root');
define('DBPASS', '');
define('DBCONNSTRING', 'mysql:host='.DBHOST.';dbname='.DBNAME);

require_once('config.php');

// Function to add fingerprint data to the database
function addFingerprintData($voterId, $fingerprintData) {
    try {
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Prepare and execute the SQL query to add fingerprint data
        $sql = "UPDATE voters SET voter_fingerprint = ? WHERE voter_id = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$fingerprintData, $voterId]);

        // Send the response as a JSON object
        $response = new stdClass();
        $response->success = true;
        echo json_encode($response);
    } catch (PDOException $e) {
        // Database error
        $error = new stdClass();
        $error->message = "Database error: " . $e->getMessage();
        echo json_encode($error);
        exit();
    }
}

// Main code
try {
    // Get the voter ID and fingerprint data from the request
    $voterId = $_POST['id'];
    $fingerprintData = base64_decode($_POST['fingerprint']);

    // Add the fingerprint data to the database
    addFingerprintData($voterId, $fingerprintData);
} catch (Exception $e) {
    // Error occurred
    $error = new stdClass();
    $error->message = "Error: " . $e->getMessage();
    echo json_encode($error);
}
?>