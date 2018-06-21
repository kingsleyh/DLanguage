package io.github.intellij.dlanguage.codeinsight.dcd

import com.google.gson.Gson
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import io.github.intellij.dlanguage.codeinsight.dcd.completions.DCDModel
import org.jetbrains.rpc.LOG
import java.util.ArrayList


data class Response(val identifiers: List<Identifier>,val type: String)
data class Identifier(val definition: String, val documentation:String, val file: String, val identifier: String,val location:Int, val type: String)

class DCDCompletionClient
{
    private val completions = ArrayList<DCDModel>()
    private val gson:Gson = Gson()

    fun autoComplete(position: Int, file: PsiFile): List<DCDModel>
    {
        val module = ModuleUtilCore.findModuleForPsiElement(file)
        completions.clear()

        if (module != null)
        {
            val server = module.getComponent(WorkspaceD::class.java)
            server.exec()

            var result = server.listCompletion(file.text, position)


            // sometimes a random character appears at the start of the string, i have no idea why
            if(result[0] != '{')
                result= result.substring(1)


            try
            {
                val response = gson.fromJson(result, Response::class.java)
                if(response.type == "identifiers")
                {
                    for (identifier in response.identifiers)
                    {
                        var type: String? = null
                        if(identifier.definition.isNotEmpty())
                        {
                            var defs = identifier.definition.split(" ")
                            if(defs.size == 2)
                                type = defs[0]
                        }
                        val model = DCDModel(identifier.identifier, identifier.type, type)
                        completions.add(model)
                    }
                }
                else
                    println("Unsupported response type: ${response.type}")
            }
            catch (e: Exception)
            {
                LOG.error(e)
            }
        }
        return completions
    }
}
