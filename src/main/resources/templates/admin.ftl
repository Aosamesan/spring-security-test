<#import "./common.ftl" as common />

<#macro printTable itemList pagingInfo>
    <#local start = pagingInfo.start + 1/>
    <#local display = pagingInfo.display />
    <#local levelItems = [
    {
        "value": "READY",
        "text": "가입 대기"
    },
    {
        "value": "BLOCK",
        "text": "차단"
    },
    {
        "value": "USER",
        "text": "유저"
    },
    {
        "value": "ADMIN",
        "text": "관리자"
    }
    ] />
    <#local authorityMap = {
        "READ": "읽기",
        "WRITE": "쓰기",
        "WRITE_INFO": "공지쓰기",
        "USER_CONFIG": "유저관리"
    } />
    <table class="table">
        <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">아이디</th>
                <th scope="col">이름</th>
                <th scope="col">레벨</th>
                <th scope="col">권한</th>
                <th scope="col">삭제</th>
            </tr>
        </thead>
        <tbody>
            <#list itemList as item>
                <tr>
                    <th scope="row">${start}</th>
                    <td>${item.username}</td>
                    <td>${item.nickname}</td>
                    <td>
                        <#if item.userLevel != "ROOT_ADMIN">
                            <div class="input-group _level_edit_form">
                                <select name="level" id="level-for-${item.username}" class="form-select _level_edit_select" aria-label="Level">
                                    <#list levelItems as levelItem>
                                        <option value="${levelItem.value}"<#if levelItem.value == item.userLevel> selected</#if>>
                                            ${levelItem.text}
                                        </option>
                                    </#list>
                                </select>
                                <button class="btn btn-outline-success _level_edit_btn" type="button" data-username="${item.username}">
                                    설정
                                </button>
                            </div>
                        <#else>
                            <input type="text" value="루트 관리자" class="form-control" aria-label="Level" disabled />
                        </#if>
                    </td>
                    <td>
                        <#list item.authorities as authority>
                            <span class="badge bg-dark">${authorityMap[authority]}</span>
                        </#list>
                    </td>
                    <td>
                        <#if item.userLevel != "ROOT_ADMIN">
                            <button class="btn btn-danger _delete_user" value="${item.username}">
                                추방
                            </button>
                        <#else>
                            <button class="btn btn-danger disabled" disabled>
                                추방 불가
                            </button>
                        </#if>
                    </td>
                </tr>
                <#local start += 1 />
            </#list>
        </tbody>
    </table>
</#macro>

<#macro printPageNavigation pagingInfo total>
    <#local start = pagingInfo.start />
    <#local display = pagingInfo.display />
    <#local currentPage = pagingInfo.currentPage />
    <#local totalPage = (total / display)?floor + 1/>
    <#local isFirst = (currentPage == 1) />
    <#local isLast = (currentPage == totalPage) />
    <#local pageStart = (currentPage > 4)?then(currentPage - 3, 1) />
    <#local pageEnd = (currentPage + 3 > totalPage)?then(totalPage, currentPage + 3) />
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item<#if isFirst> disabled</#if>">
                <a href="?start=${start - display}&display=${display}" class="page-link" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <#list (pageStart..pageEnd) as idx>
                <li class="page-item<#if idx == currentPage> active</#if>">
                    <a href="?start=${(idx - 1) * display}&display=${display}" class="page-link">${idx}</a>
                </li>
            </#list>
            <li class="page-item<#if isLast> disabled</#if>">
                <a href="?start=${start + display}&display=${display}" class="page-link">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
</#macro>


<@common.Outline title="Security Test | Admin Menu">
    <div class="d-grid">
        <@printTable DATA.itemList DATA.pagingInfo />
    </div>
    <div class="d-grid">
        <@printPageNavigation DATA.pagingInfo DATA.total />
    </div>
    <script>
        (function () {
            Array.from(document.getElementsByClassName("_level_edit_form"))
                .forEach(dom => {
                    var levelSelectBox = dom.getElementsByClassName("_level_edit_select")[0];
                    var levelSelect = dom.getElementsByClassName("_level_edit_btn")[0];
                    if (levelSelect && levelSelectBox) {
                        var username = levelSelect.getAttribute("data-username");
                        levelSelect.onclick = function() {
                            axios.put('/api/admin/users/' + username + '?level=' + levelSelectBox.value)
                                .then(() => window.location.reload());
                        };
                    }
                });
            Array.from(document.getElementsByClassName("_delete_user"))
                .forEach(dom => {
                    dom.onclick = function () {
                        axios.delete('/api/admin/users/' + dom.value)
                            .then(() => window.location.reload());
                    };
                });
        })();
    </script>
</@common.Outline>