<?php    

    $con=mysqli_connect("localhost", "root", "password", "DemTalk");
    
    if (mysqli_connect_errno($con)) {
        $response["success"] = 0;
        $response["message"] = "Connection error" . $mysqli_connect_error();
        echo json_encode($response);
    } else {
        executeQuery();
    }
    
    mysqli_close($con);

function executeQuery() {
    
    $username = $_POST["username"];
    $password = $_POST["password"];
    
    $statement = mysqli_prepare($con, "INSERT INTO 'Users' (Username, Password) VALUES (?, ?) ");
    mysqli_stmt_bind_param($statement, "ss", $username, $password);
    
    if (!(mysqli_stmt_execute($statement))) {
        $response["success"] = 0;
        $response["message"] = "Query error";
        echo json_encode($response);
    } else {
        $response["success"] = 1;
        $response["message"] = "Details added";
        echo json_encode($response);
    }
    
    mysqli_stmt_close($statement);
}
?>