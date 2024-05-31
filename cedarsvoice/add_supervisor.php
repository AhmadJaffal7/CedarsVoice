<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $id = $pdo->quote($_POST['supervisor_id']);
    $name = $pdo->quote($_POST['supervisor_name']);
    $fingerprint_data = $pdo->quote($_POST['fingerprint_data']);
    $iv = $pdo->quote($_POST['iv']);
    $police_id = $pdo->quote($_POST['police_id']);

    // Insert supervisor details, fingerprint data, and IV into the database
    $query = "INSERT INTO supervisors (supervisor_id, supervisor_name, supervisor_fingerprint, iv, police_id) 
              VALUES ($id, $name, $fingerprint_data, $iv, $police_id)";

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
