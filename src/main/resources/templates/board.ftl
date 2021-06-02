<#import "./common.ftl" as common />

<#macro printDocuments documents total pagingInfo>
    <div class="d-grid">
        <table class="table">
            <colgroup>
                <col style="width: 5%;" />
                <col style="width: 50%;" />
                <col style="width: 22.5%;" />
                <col style="width: 22.5%;" />
            </colgroup>
            <thead>
                <tr>
                    <th scope="col">#</th>
                    <th scope="col">제목</th>
                    <th scope="col">작성자</th>
                    <th scope="col">작성시각</th>
                </tr>
            </thead>
            <tbody>
                <#list documents as document>
                    <tr>
                        <th scope="row"><#if document.notice>공지<#else>${document.displayId}</#if></th>
                        <td>
                            <div class="d-flex justify-content-between">
                                <a href="/board/${document.displayId}" class="link-secondary">${document.title?html}</a> <span class="badge rounded-pill bg-dark">${document.replyCount}</span>
                            </div>
                        </td>
                        <td>${document.authorNickname?html}</td>
                        <td>${document.createdAt?string('yyyy년 M월 d일 HH:mm')}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</#macro>

<#macro printButtons>
    <#if CURRENT_USER.authorities?seq_contains("WRITE")>
        <div class="d-grid justify-content-end">
            <div class="input-group">
                <a href="/board/new" class="btn btn-success">새 글 작성</a>
            </div>
        </div>
    </#if>
</#macro>


<@common.Outline title="게시판">
    <@printDocuments DATA.itemList DATA.total DATA.pagingInfo />
    <@common.printPageNavigation DATA.pagingInfo DATA.total />
    <@printButtons />
</@common.Outline>