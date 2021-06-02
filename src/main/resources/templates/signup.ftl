<#import "./common.ftl" as common />

<@common.Outline title="가입하기">
    <form class="form-control" onsubmit="return false;">
        <div class="mb-3">
            <label for="username" class="form-label">아이디</label>
            <input type="text" id="username" class="form-control" name="username" required/>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" id="password" class="form-control" name="password" required/>
        </div>
        <div class="mb-3">
            <label for="confirm-password" class="form-label">비밀번호 확인</label>
            <input type="password" id="confirm-password" class="form-control" name="confirmPassword" required/>
        </div>
        <div class="mb-3">
            <label for="nickname" class="form-label">별명</label>
            <input type="text" id="nickname" class="form-control" name="nickname" required/>
        </div>
        <div class="d-grid mb-3">
            <button id="submit-button" name="submitButton" class="btn btn-outline-success" type="button">Sign in</button>
        </div>
        <script>
            (function() {
                var password = document.getElementById("password");
                var confirmPassword = document.getElementById("confirm-password");
                var submitButton = document.getElementById("submit-button");

                function checkPassword() {
                    if (password.value && password.value === confirmPassword.value) {
                        return true;
                    }
                    alert("비밀번호가 일치하지 않음");
                    return false;
                }

                submitButton.onclick = function() {
                    if (!checkPassword()) {
                        return false;
                    }
                    var username = document.getElementById("username");
                    var nickname = document.getElementById("nickname");
                    var params = new URLSearchParams();
                    params.append("username", username.value);
                    params.append("nickname", nickname.value);
                    params.append("password", password.value);
                    axios.post('/api/users', params).then(
                        () => window.location.href = "/"
                    )
                };
            })();
        </script>
    </form>
</@common.Outline>