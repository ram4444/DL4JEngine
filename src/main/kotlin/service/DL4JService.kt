package main.kotlin.service

import main.kotlin.dl4j.SimpleModel
import main.kotlin.pojo.TrainingEvalResult
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.springframework.stereotype.Service
import java.net.URI
import org.nd4j.linalg.factory.Nd4j



@Service
class DL4JService {
    private val logger = KotlinLogging.logger {}

    var simpleModelList:MutableMap<String,SimpleModel> = mutableMapOf()

    fun importModel(path: String)  {
        val simpleModel = SimpleModel(URI(path))
        simpleModel.initByImport()
        simpleModelList[path] = simpleModel
    }

    fun createSimpleModel(path: String, labelIndex:Int, numClasses:Int, batchSize:Int, seed:Long)  {
        val simpleModel = SimpleModel(URI(path))
        simpleModel.initByConf(labelIndex, numClasses, batchSize, seed)
        simpleModelList[path] = simpleModel
    }

    fun trainModel(path:String,
            jsonPath:String,
            trainingBatchSize:Int,
            labelIndex:Int,
            numClasses:Int,
            fitTimes:Int,
            evalMetric:String,
            passRate:Double): TrainingEvalResult {
        var trainingEvalResult = TrainingEvalResult(ObjectId.get().toString(),path,0F,0F,0F,0F)
        try {
            trainingEvalResult = simpleModelList[path]!!.trainByJSON(
                    jsonPath,
                    trainingBatchSize,
                    labelIndex,
                    numClasses,
                    fitTimes,
                    evalMetric,
                    passRate)
        } catch (e:Exception) {
            logger.error{"DL4JModel $path not found"}
        }
        return trainingEvalResult
    }

    fun queryModel(path:String, input:String):Int {
        val features = Nd4j.create(listOf(input))
        //val labels = Nd4j.create(listOf(input))
        //var testData = DataSet(features, labels)
        var output = simpleModelList.get(path)!!.model.output(features)
        return  output.getInt(0)
    }


}