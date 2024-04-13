<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $id = $pdo->quote($_POST['id']);
    $name = $pdo->quote($_POST['name']);
    $fingerprint_data = $pdo->quote($_POST['fingerprint_data']);

    // $fingerprint_data = base64_decode($fingerprint_data);

    // Insert voter details and fingerprint data into the database
    $query = "INSERT INTO supervisors (supervisor_id, supervisor_name, supervisor_fingerprint) 
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
