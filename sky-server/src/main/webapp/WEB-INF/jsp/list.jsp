<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>

<c:choose>
    <c:when test="${fn:length(clientVoMap) == 0}">
        <div class="group">
            <form>
                <fieldset>
                    <legend>请接入客户端</legend>
                </fieldset>
            </form>
        </div>
    </c:when>
    <c:otherwise>
        <c:forEach items="${clientVoMap}" var="groupInfo">

            <div class="group">
                <form>
                    <fieldset>
                        <legend>系统分组号:${groupInfo.key} totalNode(总节点数):${fn:length(groupInfo.value)}</legend>

                        <c:forEach items="${groupInfo.value}" var="clientVo">
                            <p>
                                <label>客户端id:${clientVo.id} </label>-->nodeNum(任务编号):${clientVo.nodeNum}
                            </p>
                        </c:forEach>

                    </fieldset>
                </form>
            </div>
        </c:forEach>
    </c:otherwise>
</c:choose>
