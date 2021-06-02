<#import "./common.ftl" as common />

<@common.Outline title="${DOCUMENT.title} 수정">
    <#assign hasNoticeAuth = CURRENT_USER.authorities?seq_contains("WRITE_INFO") />
    <form class="form-control" onsubmit="return false;">
        <label for="title" class="form-label">제목</label>
        <input type="text" name="title" id="title" class="form-control" value="${DOCUMENT.title?html}" />
        <label for="content" class="form-label">내용</label>
        <textarea type="text" name="content" id="content" class="form-control noresize" rows="25">${DOCUMENT.content?html}</textarea>
        <#if hasNoticeAuth>
            <div class="form-check">
                <input id="notice" type="checkbox" class="form-check-input" name="notice" <#if DOCUMENT.notice>checked</#if>/>
                <label for="notice" class="form-check-label">공지</label>
            </div>
        </#if>
        <div class="input-group mt-2">
            <button class="btn btn-success" id="edit-button" type="button">수정</button>
        </div>
    </form>
    <script>
        (function() {
            var titleDOM = document.getElementById("title");
            var contentDOM = document.getElementById("content");
            var addButton = document.getElementById("edit-button");
            <#if hasNoticeAuth>
            var noticeCheckDOM = document.getElementById("notice");
            </#if>

            function validateForm() {
                if (!titleDOM.value) {
                    alert("타이틀이 비어있습니다.");
                    return false;
                }
                if (!contentDOM.value) {
                    alert("내용이 비어있습니다.");
                    return false;
                }
                return true;
            }

            addButton.onclick = function (e) {
                if (validateForm()) {
                    var params = new URLSearchParams();
                    params.append("title", titleDOM.value);
                    params.append("content", contentDOM.value);
                    <#if hasNoticeAuth>
                    params.append("isNotice", noticeCheckDOM.checked);
                    </#if>
                    axios.put('/api/documents/${DOCUMENT.displayId}', params)
                        .then(res => res.data)
                        .then(() => window.location.href = '/board/${DOCUMENT.displayId}');
                }
            };
        })();
    </script>
</@common.Outline>