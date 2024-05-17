<?php
define('DBHOST', 'localhost');
define('DBNAME', 'cedars_voice');
define('DBUSER', 'root');
define('DBPASS', '');
define('DBCONNSTRING', 'mysql:host='.DBHOST.';dbname='.DBNAME);

require_once('config.php');

// Function to compare fingerprint data
function compareFingerprint($scannedFingerprintData, $storedFingerprintData) {
    // Implement your comparison logic here
    // For demonstration purposes, a simple byte-to-byte comparison is used
    return ($scannedFingerprintData === $storedFingerprintData);
}

// Function to retrieve fingerprint data from the database
function retrieveFingerprintData($voterId) {
    try {
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Prepare and execute the SQL query to retrieve fingerprint data
        $sql = "SELECT voter_fingerprint FROM voters WHERE voter_id = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$voterId]);

        // Check if the query returned a result
        if ($stmt->rowCount() > 0) {
            // Fetch the row and return the fingerprint data
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            return $row['voter_fingerprint'];
        } else {
            // Fingerprint data not found
            return null;
        }
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
    // Get the voter ID from the request
    $voterId = $_POST['id'];

    // Retrieve the scanned fingerprint data from the request (assuming it's passed as a base64 encoded string)
    $scannedFingerprintData = base64_decode($_POST['fingerprint']);

    // Retrieve the stored fingerprint data from the database
    $storedFingerprintData = retrieveFingerprintData($voterId);

    // Check if the stored fingerprint data is retrieved successfully
    if ($storedFingerprintData !== null) {
        // Compare the scanned fingerprint data with the stored fingerprint data
        $fingerprintMatch = compareFingerprint($scannedFingerprintData, $storedFingerprintData);

        // Send the response as a JSON object
        $response = new stdClass();
        $response->fingerprintMatch = $fingerprintMatch;
        echo json_encode($response);
    } else {
        // Fingerprint data not found in the database
        $error = new stdClass();
        $error->message = "Fingerprint data not found in the database for the given voter ID";
        echo json_encode($error);
    }
} catch (Exception $e) {
    // Error occurred
    $error = new stdClass();
    $error->message = "Error: " . $e->getMessage();
    echo json_encode($error);
}
?>