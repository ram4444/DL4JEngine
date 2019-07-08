package main.kotlin.dl4j

import main.kotlin.pojo.TrainingEvalResult
import mu.KotlinLogging
import org.datavec.api.records.reader.RecordReader
import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.records.reader.impl.jackson.FieldSelection
import org.datavec.api.records.reader.impl.jackson.JacksonLineRecordReader
import org.datavec.api.split.FileSplit
import org.datavec.api.util.ClassPathResource
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.SplitTestAndTrain
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize
import org.nd4j.linalg.learning.config.Sgd
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.shade.jackson.core.JsonFactory
import org.nd4j.shade.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.bson.types.ObjectId
import java.io.File
import java.net.URI


class SimpleModel(
        val modelPath: URI      //Where to save the network. Note: the file is in .zip format - can be opened externally
        ) {
    private val logger = KotlinLogging.logger {}
    lateinit var model:MultiLayerNetwork

    fun initByImport() {
        model = MultiLayerNetwork.load(File("/home/ram4444/IdeaProjects/DL4JEngine/src/main/resources/"+modelPath), true)
    }

    fun initByConf(
            labelIndex: Int,     //Location of the class label. eg. 5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
            numClasses: Int,     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
            batchSize: Int,      //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)
            seed: Long
    ) {

        val numInputs = labelIndex
        val outputNum = numClasses

        Activation.RELU
        logger.info("Build model....")
        var conf:MultiLayerConfiguration = NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(Sgd(0.1))
                .l2(1e-4)
                .list()
                .layer(DenseLayer.Builder().nIn(numInputs).nOut(3)
                        .build())
                .layer(DenseLayer.Builder().nIn(3).nOut(3)
                        .build())
                .layer( OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(3).nOut(outputNum).build())
                .build()

        model = MultiLayerNetwork(conf)
        model.init()
        model.setListeners(ScoreIterationListener(100))

    }

    fun trainByJSON(jsonPathName:String, trainingBatchSize:Int ,labelIndex:Int ,numClasses:Int, fitTimes:Int, evalMetric:String, passRate:Double): TrainingEvalResult{
        val mapper = ObjectMapper(JsonFactory())
        val fieldSelection = FieldSelection.Builder().addField("value1").
                addField("value2").
                addField("value3").
                addField("value4").
                addField("value5").build()
        val recordReader = JacksonLineRecordReader(fieldSelection, mapper)
        recordReader.initialize(FileSplit(ClassPathResource(jsonPathName).getFile()))
        val iterator = RecordReaderDataSetIterator(recordReader,trainingBatchSize,labelIndex,numClasses)
        var allData:DataSet = iterator.next()
        allData.shuffle()
        var testAndTrain = allData.splitTestAndTrain(0.6)
        var trainingData = testAndTrain.getTrain()
        var testData = testAndTrain.getTest()
        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        var normalizer:DataNormalization = NormalizerStandardize()
        normalizer.fit(trainingData)           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData)     //Apply normalization to the training data
        normalizer.transform(testData)         //Apply normalization to the test data. This is using statistics calculated from the *training* set

        for (x in 0..fitTimes) {
            model.fit(trainingData)
        }
        var pass = false
        var eval = Evaluation(numClasses)
        var output = model.output(testData.features)
        eval.eval(testData.getLabels(), output)
        //logger.info(output.toString())
        logger.info(eval.stats())

        when(evalMetric){
            "accuracy" -> if (eval.accuracy()>passRate) pass=true
            "precision" -> if (eval.precision()>passRate) pass=true
            "recall" -> if (eval.recall()>passRate) pass=true
            "f1" -> if (eval.f1()>passRate) pass=true
        }

         if(pass) {
             val saveUpdater = true                 //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
             model.save(File("/home/ram4444/IdeaProjects/DL4JEngine/src/main/resources/"+modelPath), saveUpdater)
             logger.info("TrainingPass")
         } else {
             logger.info("TrainingNotPass")
         }

        val trainingEvalResult = TrainingEvalResult(
                ObjectId.get().toString(),
                this.modelPath.toString(),
                eval.accuracy().toFloat(),
                eval.precision().toFloat(),
                eval.recall().toFloat(),
                eval.f1().toFloat())

        return trainingEvalResult
    }

}