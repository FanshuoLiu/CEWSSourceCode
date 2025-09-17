<%--
  Created by IntelliJ IDEA.
  User: lfs
  Date: 2024/9/21
  Time: 14:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录</title>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
</head>
<body>
<h2>用户登录</h2>
<form action="" id="loginForm" method="post">
    <input type="hidden" name="action" value="login" id="action"/>
    用户名: <input type="text" name="username" id="username" required/><br/>
    密码: <input type="password" name="password" id="password" required/><br/>
    <input type="button" id="login" value="登录"/>
</form>
<p><a href="register.jsp">注册新用户</a></p>
<script>
    $(document).ready(function () {
        $("#login").click(function () {
            const username = $("#username").val();
            const password = $("#password").val();
            const action=$("#action").val();
            console.log(action);
            console.log(username);
            if (username == "" || password == ""){
                alert("请输入完整信息");
                return;
            }
            $.ajax({
                url: 'UserServlet',
                data: {
                    username: username,
                    password: password,
                    action:action
                },
                dataType: 'json',
                type: 'POST',
                success: function (data) {
                    if (data.status=="success") {
                        alert(data.message);
                        window.location.href = "index.jsp";
                    } else if (data.status=="error"){
                        alert("登陆失败:"+data.message);
                    }
                }
            })
})
})

</script>
</body>
</html>
