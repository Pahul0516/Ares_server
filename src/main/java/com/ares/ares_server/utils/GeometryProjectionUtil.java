package com.ares.ares_server.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class GeometryProjectionUtil {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    private static final CoordinateReferenceSystem WGS84 = crsFactory.createFromName("EPSG:4326");
    private static final CoordinateReferenceSystem UTM34N = crsFactory.createFromName("EPSG:32634");

    /**
     * Transform geometry from WGS84 to UTM (meters)
     */
    public static Geometry toUTM(Geometry geom) {
        return transform(geom, WGS84, UTM34N);
    }

    /**
     * Transform geometry from source CRS to target CRS
     */
    private static Geometry transform(Geometry geom, CoordinateReferenceSystem src, CoordinateReferenceSystem dst) {
        CoordinateTransform transform = ctFactory.createTransform(src, dst);

        GeometryTransformer geometryTransformer = new GeometryTransformer() {
            @Override
            protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
                Coordinate[] result = new Coordinate[coords.size()];
                for (int i = 0; i < coords.size(); i++) {
                    ProjCoordinate srcCoord = new ProjCoordinate(coords.getX(i), coords.getY(i));
                    ProjCoordinate dstCoord = new ProjCoordinate();
                    transform.transform(srcCoord, dstCoord);
                    result[i] = new Coordinate(dstCoord.x, dstCoord.y);
                }
                return new CoordinateArraySequence(result);
            }
        };

        return geometryTransformer.transform(geom);
    }

    public static Geometry bufferInMeters(Geometry geom, double meters) {
        Geometry projected = toUTM(geom);
        Geometry buffered = projected.buffer(meters);
        // back to WGS84
        return transform(buffered, UTM34N, WGS84);
    }
}
