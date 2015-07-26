<?php

try {
    $db = new PDO('mysql:host=localhost;dbname=DemTalk;charset=utf8', 'root', 'password');
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    $response["success"] = false;
    $response["message"] = "Connection failed";
    echo json_encode($response);
    $stmt = $db = null;
}   

$response = array();

if(isset($_POST["username"]) && isset($_POST["password"])) 
{    
    $username = $_POST["username"];
    $password = $_POST["password"];
    $hash = password_hash($password, PASSWORD_BCRYPT);
    
    $stmt = $db->prepare("INSERT INTO Users (username, password) VALUES (:username, :password) ");
    try {    
        if($stmt->execute(array(':username' => $username, ':password' => $hash))) {
            $response["success"] = true;
            $response["message"] = "User details added";
        }  
    } catch (PDOException $ex) { 
        $response["success"] = false;
        $response["message"] = "Query failed"; 
    }
    

} 
else {
    $response["success"] = false;
    $response["message"] = "Values not found";
}

    echo json_encode($response);
    $stmt = $db = null;

?>