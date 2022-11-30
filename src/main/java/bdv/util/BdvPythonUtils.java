/*
 * #%L
 * BigDataViewer core classes with minimal dependencies.
 * %%
 * Copyright (C) 2012 - 2022 BigDataViewer developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bdv.util;

/**
 * Some utils to support Python interop
 *
 * @author Kyle Harrington
 *
 * Some snippets are from bigdataviewer/bigdataviewer-vistools:bdv.BdvFunction
 */
public class BdvPythonUtils
{
    public static < T > BdvStackSource< T > getStackSource(final RandomAccessibleInterval< T > img,
                                                           final String name) {
        return getStackSource(img, name, Bdv.options());
    }

    public static < T > BdvStackSource< T > getStackSource(final RandomAccessibleInterval< T > img,
                                                           final String name,
                                                           final BdvOptions options ) {
        final BdvHandle handle = getHandle( options );
        final AxisOrder axisOrder = AxisOrder.getAxisOrder( options.values.axisOrder(), img, handle.is2D() );
        final AffineTransform3D sourceTransform = options.values.getSourceTransform();
        final T type;
        if ( img instanceof VolatileView )
            {
                final VolatileViewData< ?, ? > viewData = ( ( VolatileView< ?, ? > ) img ).getVolatileViewData();
                type = ( T ) viewData.getVolatileType();
                handle.getCacheControls().addCacheControl( viewData.getCacheControl() );
            }
        else
            type = Util.getTypeFromInterval( img );
        
        return addRandomAccessibleInterval( handle, ( RandomAccessibleInterval ) img, ( NumericType ) type, name, axisOrder, sourceTransform );
    }

    /**
     * Get existing {@code BdvHandle} from {@code options} or create a new
     * {@code BdvHandleFrame}.
     */
    private static BdvHandle getHandle( final BdvOptions options )
    {
        final Bdv bdv = options.values.addTo();
        return ( bdv == null )
            ? new BdvHandleFrame( options )
            : bdv.getBdvHandle();
    }
    
}
