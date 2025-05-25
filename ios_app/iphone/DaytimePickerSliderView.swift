import SwiftUI
import shared

private let hPadding = H_PADDING + 1

// Slider line a little wider to nice ui
private let sliderInternalPadding = 3.0
private let circleSize = 24.0
private let circleDefaultOffset = hPadding - (circleSize / 2) + sliderInternalPadding

// No size of tick, but for absolute positioned view
private let tickNoteWidthDp = 20.0

struct DaytimePickerSliderView: View {

    let daytimeUi: DaytimeUi
    let onChange: (DaytimeUi) -> Void

    var body: some View {

        let sliderUi = DaytimePickerSliderUi(
            hour: daytimeUi.hour,
            minute: daytimeUi.minute
        )

        VStack {

            SliderView(
                tickIdx: sliderUi.hour.toInt(),
                ticks: sliderUi.hourTicks,
                stepTicks: sliderUi.hourStepSlide.toInt(),
                onChange: { hour in
                    let newDaytimeUi = DaytimeUi(
                        hour: hour.toInt32(),
                        minute: daytimeUi.minute
                    )
                    onChange(newDaytimeUi)
                }
            )
            .padding(.top, 4)

            SliderView(
                tickIdx: sliderUi.minute.toInt(),
                ticks: sliderUi.minuteTicks,
                stepTicks: sliderUi.minuteStepSlide.toInt(),
                onChange: { minute in
                    let newDaytimeUi = DaytimeUi(
                        hour: daytimeUi.hour,
                        minute: minute.toInt32()
                    )
                    onChange(newDaytimeUi)
                }
            )
            .padding(.top, 14)
            .padding(.bottom, 8)
        }
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

    @State private var circleOffsetAnimation = 0.0

    var body: some View {

        ZStack(alignment: .topLeading) {

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
                .background(roundedShape.fill(c.dividerBg))
                .padding(.horizontal, hPadding)

                //
                // Circle
                // Ignore init animation

                if sliderXPx > 0 {

                    let circleOffset = circleDefaultOffset + (tickAxmPx * tickIdx.toDouble())

                    HStack {

                        HStack {

                            Text(ticks[tickIdx].text)
                                .foregroundColor(c.text)
                                .font(.system(size: 12, weight: .medium))
                        }
                        .frame(width: circleSize, height: circleSize, alignment: .center)
                        .background(roundedShape.fill(.blue))
                        .offset(x: circleOffsetAnimation)

                        Spacer()
                    }
                    .animateVmValue(
                        value: circleOffset,
                        state: $circleOffsetAnimation,
                        animation: .spring(response: 0.15)
                    )
                }

                ////
            }
            .frame(height: circleSize)
            .zIndex(1.0)

            ZStack(alignment: .top) {

                ForEachIndexed(ticks) { tickIdx, tick in

                    let tickNoteHalfPx = tickNoteWidthDp / 2
                    let offsetX = (tickAxmPx * tickIdx.toDouble()) - tickNoteHalfPx

                    ZStack(alignment: .top) {

                        if tick.withSliderStick {
                            ZStack {
                            }
                            .frame(width: 1, height: 5)
                            .background(c.dividerFg)
                        }

                        if let sliderStickText = tick.sliderStickText {
                            Text(sliderStickText)
                                .padding(.top, 8)
                                .foregroundColor(.secondary)
                                .font(.system(size: 13, weight: .light))
                        }
                    }
                    .offset(x: offsetX)
                    .frame(width: tickNoteWidthDp)
                }
            }
            .zIndex(0.0)
            .padding(.top, 18)
            .padding(.horizontal, hPadding + sliderInternalPadding)
        }
        .highPriorityGesture(gesture)
    }

    var gesture: some Gesture {

        DragGesture(minimumDistance: 0, coordinateSpace: .global)
            .onChanged { value in
                let slideXPosition = value.location.x - sliderXPx
                let stepPx = tickAxmPx * stepTicks.toDouble()

                let newIdx = DaytimePickerSliderUi.companion.calcSliderTickIdx(
                    ticksSize: ticks.count.toInt32(),
                    stepTicks: stepTicks.toInt32(),
                    slideXPosition: Float(slideXPosition),
                    stepPx: Float(stepPx)
                )

                if tickIdx != newIdx {
                    onChange(newIdx.toInt())
                }
            }
    }
}
