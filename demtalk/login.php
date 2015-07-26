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
    
        try {
            $stmt = $db->prepare("SELECT * FROM Users WHERE username=?");
            $stmt->execute(array($username));
            $results = $stmt->fetch(PDO::FETCH_ASSOC);
         } catch (PDOException $e) {
                $response["success"] = false;
                $response["message"] = "Query failed";
                echo json_encode($response);
                $stmt = $db = null;
        }
    
            if($results == null) {
                $response["success"] = false;
                $response["message"] = "Username not found";
            } else {
                    $hash = $results["password"];
                    if(password_verify($password, $hash)) {
                        $response["success"] = true;
                        $response["message"] = "Log user in";   
                    } else {
                        $response["success"] = false;
                        $response["message"] = "Password doesn't match";  
                    }
            }

}   
else 
{
    $response["success"] = false;
    $response["message"] = "Values not found";
}

echo json_encode($response);
$stmt = $con = null; 
?>