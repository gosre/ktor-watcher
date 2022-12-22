<#-- @ftlvariable name="watchers" type="kotlin.collections.List<me.claytonw.watcher.Watcher>" -->
<#import "_layout.ftl" as layout />
<@layout.header>
    <#list watchers?reverse as watcher>
        <div>
            <h3>
                ${watcher.target.name} - ${watcher.target.host}
            </h3>
        </div>
    </#list>
    <hr>
</@layout.header>