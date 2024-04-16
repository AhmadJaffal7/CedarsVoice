<?php
// Include the config.php file
require_once 'config.php';

// Create connection
$conn = new PDO(DBCONNSTRING, DBUSER, DBPASS);

// Check connection
if (!$conn) {
    die("Connection failed: " . $conn->errorInfo());
}

// Check if the 'voter_id' key exists in the $_GET array
if (isset($_GET['voter_id'])) {
    // Get the voter ID from the query parameters
    $id = $_GET['voter_id'];

    // Prepare and bind
    $stmt = $conn->prepare("SELECT voter_id FROM voters WHERE voter_id = ?");
    $stmt->bindParam(1, $id, PDO::PARAM_INT);

    // Execute the statement
    $stmt->execute();

    // Fetch the result
    $storedId = $stmt->fetchColumn();

    // Check if the ID exists and return a JSON response
    if ($storedId) {
        echo json_encode(array("idExists" => true));
    } else {
        echo json_encode(array("idExists" => false));
    }
} else {
    // If 'voter_id' is not set in the URL parameters, return an error response
    echo json_encode(array("error" => "voter_id parameter is missing"));
}
?>
