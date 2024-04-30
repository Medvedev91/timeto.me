import SwiftUI
import shared

// Slider line a little wider to nice ui
private let sliderInternalPadding = 3.0
private let circleSize = 22.0
private let circleDefaultOffset = H_PADDING - (circleSize / 2) + sliderInternalPadding

// No size of tick, but for absolute positioned view
private let tickNoteWidthDp = 16.0

struct DaytimePickerSliderView: View {

    let daytimeModel: DaytimeModel
    let onChange: (DaytimeModel) -> Void

    var body: some View {

        let sliderUi = DaytimePickerSliderUi(
            hour: daytimeModel.hour,
            minute: daytimeModel.minute
        )

        SliderView(
            tickIdx: sliderUi.hour.toInt(),
            ticks: sliderUi.hourTicks,
            stepTicks: sliderUi.hourStepSlide.toInt(),
            onChange: { hour in
                let newDaytimeModel = DaytimeModel(
                    hour: hour.toInt32(),
                    minute: daytimeModel.minute
                )
                onChange(newDaytimeModel)
            }
        )
    }
}

private struct SliderView: View {

    let tickIdx: Int
    let ticks: [DaytimePickerSliderUi.Tick]
    let stepTicks: Int
    let onChange: (Int) -> Void

    // 0 also used as "no value"
    @State private var sliderXPx = 0.0
    // Not exact but approximate `slide width` / `ticks size`
    @State private var tickAxmPx = 0.0

    var body: some View {

        ZStack {

            ZStack {

                ZStack {
                }
                .frame(minWidth: 0, maxWidth: .infinity)
                .frame(height: 4)
                .background(GeometryReader { geometry -> Color in
                    myAsyncAfter(0.01) {
                        let frame = geometry.frame(in: CoordinateSpace.global)
                        let newSliderXPx = frame.origin.x
                        // "ticks.size - 1" to make first and last sticks on edges
                        let newTickPx = geometry.size.width / (ticks.count - 1).toDouble()
                        if !(sliderXPx == newSliderXPx && tickAxmPx == newTickPx) {
                            sliderXPx = newSliderXPx
                            tickAxmPx = newTickPx
                        }
                    }
                    return Color.clear
                })
                .padding(.horizontal, sliderInternalPadding)
                .background(roundedShape.fill(c.sheetFg))
                .padding(.horizontal, H_PADDING)

                //
                // Circle
                // Ignore init animation

                if sliderXPx > 0 {

                    let circleOffset = circleDefaultOffset + (tickAxmPx * tickIdx.toDouble())

                    ZStack {
                    }
                    .offset(x: circleOffset)
                    .frame(width: circleSize, height: circleSize)
                    .background(roundedShape.fill(c.blue))
                }

                ////
            }
            .frame(height: circleSize)
        }
    }
}
