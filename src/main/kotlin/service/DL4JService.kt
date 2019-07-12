package main.kotlin.service

import main.kotlin.dl4j.SimpleModel
import main.kotlin.pojo.SimpleModelResult
import main.kotlin.pojo.TrainingEvalResult
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize
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

    fun queryModel(path:String, value1:String, value2:String, value3:String, value4:String):SimpleModelResult {
        //val input = listOf(value1.toFloat(), value2.toFloat(), value3.toFloat(), value4.toFloat())
        //val inputArr = mutableListOf(input)
        val arr= arrayOf(floatArrayOf(value1.toFloat(), value2.toFloat(), value3.toFloat(), value4.toFloat()))
        val features = Nd4j.create(arr)
        //TODO: label should be defined in elsewhere for each model
        val arr2= arrayOf(longArrayOf(990, 991, 992))
        val labels = Nd4j.create(arr2)
        val dataSet = DataSet(features, labels)
        dataSet.labelNames = listOf("ans0","ans1","ans2")
        println("------------------------queryModel----------------------")
        println(dataSet.features)
        //var normalizer: DataNormalization = NormalizerStandardize()
        //normalizer.fit(dataSet)
        //normalizer.transform(dataSet)
        //for (x in 0..1000) {
            //simpleModelList.get(path)!!.model.fit(dataSet)
        //}


        //val labels = Nd4j.create(listOf(input))
        //var testData = DataSet(features, labels)
        println("------------------------output----------------------")
        var output = simpleModelList.get(path)!!.model.output(dataSet.features)
        println(output)
        println("result:"+output.getRow(0).argMax())
        println("confidence:"+output.getRow(0).getFloat(output.getRow(0).argMax().getLong()))
        var predict = simpleModelList.get(path)!!.model.predict(dataSet.features)
        println(predict)
        //logger.info(testData.getLabelName(output.getRow(1).argMax().getInt()))
        var result = SimpleModelResult(path, "", predict[0], output.getRow(0).getFloat(output.getRow(0).argMax().getLong()))
        return result
    }


}