<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $id = $pdo->quote($_POST['police_id']);
    $name = $pdo->quote($_POST['police_name']);
    $fingerprint_data = $pdo->quote($_POST['fingerprint_data']);

    // $fingerprint_data = base64_decode($fingerprint_data);

    // Insert voter details and fingerprint data into the database
    $query = "INSERT INTO police (police_id, police_name, police_fingerprint) 
              VALUES ($id, $name, $fingerprint_data)";

    if ($pdo->exec($query)) {
        echo "success";
    } else {
        echo "fail";
    }

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>