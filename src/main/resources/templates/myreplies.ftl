<#import "./common.ftl" as common />

<@common.Outline title="내 댓글">
    <div class="d-grid">
        <table class="table">
            <tbody>
                <tr>
                    <th scope="col">내용</th>
                    <th scope="col">작성일시</th>
                </tr>
            </tbody>
            <tbody>
                <#list DATA.itemList as reply>
                    <tr>
                        <th scope="row">
                            <a href="/board/${reply.documentDisplayId}" class="link-secondary">${reply.content}</a>
                        </th>
                        <td>${reply.createdAt?string('yyyy년 M월 H일 HH:mm')}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
    <@common.printPageNavigation DATA.pagingInfo DATA.total />
</@common.Outline>