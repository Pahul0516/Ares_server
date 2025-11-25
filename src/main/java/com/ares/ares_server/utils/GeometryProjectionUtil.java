package com.ares.ares_server.utils;

import org.locationtech.jts.geom.Geometry;
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

    public static Geometry bufferInMeters(Geometry geom, double meters) {
        // Transform WGS84 → UTM (meters)
        Geometry projected = transform(geom, WGS84, UTM34N);

        // Buffer in meters
        Geometry buffered = projected.buffer(meters);

        // Back to WGS84
        return transform(buffered, UTM34N, WGS84);
    }

    private static Geometry transform(Geometry geom, CoordinateReferenceSystem src, CoordinateReferenceSystem dst) {
        CoordinateTransform transform = ctFactory.createTransform(src, dst);

        GeometryTransformer geometryTransformer = new GeometryTransformer() {
            private org.locationtech.jts.geom.Coordinate transformCoordinate(org.locationtech.jts.geom.Coordinate coord, Geometry parent) {
                ProjCoordinate input = new ProjCoordinate(coord.x, coord.y);
                ProjCoordinate output = new ProjCoordinate();

                transform.transform(input, output);

                return new org.locationtech.jts.geom.Coordinate(output.x, output.y);
            }
        };

        return geometryTransformer.transform(geom);
    }
}
