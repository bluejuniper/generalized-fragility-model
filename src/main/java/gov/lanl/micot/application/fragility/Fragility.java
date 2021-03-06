package gov.lanl.micot.application.fragility;

import gov.lanl.micot.application.fragility.core.GFMEngine;
import gov.lanl.micot.application.fragility.core.GeometryObject;
import gov.lanl.micot.application.fragility.core.ResponseEstimatorFactory;
import gov.lanl.micot.application.fragility.io.GFMDataReader;
import gov.lanl.micot.application.fragility.responseEstimators.ResponseEstimator;
import gov.lanl.micot.application.utilities.asset.PropertyData;
import gov.lanl.micot.application.utilities.gis.HazardField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fragility {

    private static FragilityParameters parser;

    public static void main(String[] args) {

        // parse arguments
        parser = new FragilityParameters(args);

        if (args.length < 1) {
            System.err.println("no arguments provided - use help 'h' option");
            System.exit(1);
        }

        mainRoutine();

    }

    /**
     * Main Fragility calling routine
     */
    private static void mainRoutine() {

        String output = parser.getOutputFilePath();
        String estimator = parser.getResponseEstimator();

        // hazards
        String[] hazardFiles = parser.getHazardInputPaths();
        String[] ids = parser.getIdentifiers();
        String assets = parser.getAssetInputPath();
        GFMDataReader gfmDataReader = new GFMDataReader();
        ArrayList<HazardField> hazardObjects = gfmDataReader.readHazardFile(hazardFiles, ids);

        // assets
        gfmDataReader.readGeoJsonFile(assets);
        ArrayList<GeometryObject> dataAssets = gfmDataReader.getGeometryObjects();
        List<Map<String, PropertyData>> props = gfmDataReader.getProperties();

        // GFM set-up and produce exposures
        GFMEngine broker = new GFMEngine();
        broker.setHazardfields(hazardObjects);
        broker.setGeometryObjects(dataAssets);
        broker.setAssetProperties(props);
        broker.produceExposures();

        // compute response approximations
        ResponseEstimatorFactory ref = new ResponseEstimatorFactory();
        ResponseEstimator r1 = ref.runResponseEstimator(estimator, broker, output);
        r1.writeResults();

    }

}
