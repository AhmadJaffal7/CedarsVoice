<?php


require_once('config.php');

// Function to compare fingerprint data
function compareFingerprint($scannedFingerprintData, $storedFingerprintData)
{
    // Compare the scanned fingerprint data with the stored fingerprint data
    // Since the fingerprint data is stored as a BLOB, we can do a direct byte-by-byte comparison
    return ($scannedFingerprintData === $storedFingerprintData);
}

// Function to retrieve fingerprint data from the database
function retrieveFingerprintData($voterId)
{
    try {
        $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Prepare and execute the SQL query to retrieve fingerprint data
        $sql = "SELECT voter_fingerprint FROM voters WHERE voter_id = :voter_id";
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':voter_id', $voterId, PDO::PARAM_INT);
        $stmt->execute();

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
        error_log("Database error: " . $e->getMessage());
        return null;
    }
}

// Main code
try {
    // Get the voter ID from the request
    $voterId = filter_var($_POST['id'], FILTER_VALIDATE_INT);

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
    error_log("Error: " . $e->getMessage());
    $error = new stdClass();
    $error->message = "An error occurred while processing the request";
    echo json_encode($error);
}