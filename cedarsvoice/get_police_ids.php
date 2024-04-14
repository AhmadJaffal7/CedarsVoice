<?php
// Include the configuration file
require_once('config.php');

try {
    // Establish a database connection using PDO
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // SQL query to select all police IDs from the police table
    $getAllPoliceIDs = "SELECT police_id FROM police";

    // Execute the query
    $result = $pdo->query($getAllPoliceIDs);

    // Array to store the retrieved police IDs
    $return_array = array();

    // Loop through the result set
    while ($row = $result->fetch()) {
        // Create an associative array for each row with police_id as the key
        $police_id = $row['police_id'];

        // Add the police_id to the return array
        $return_array[] = $police_id;
    }

    // Set the response header to indicate JSON content
    header('Content-Type: application/json');

    // Encode the return array as JSON and output it
    echo json_encode($return_array);

    // Close the database connection
    $pdo = null;
} catch (PDOException $e) {
    // Handle any PDO exceptions
    die($e->getMessage());
}
?>
