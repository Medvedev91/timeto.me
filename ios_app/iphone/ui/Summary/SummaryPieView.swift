import SwiftUI
import shared

struct SummaryPieView: View {
    
    let separatorDegrees = 8.0
    let ringRatio = 2.1
    
    let selectedId: String?
    let onIdSelected: (String?) -> Void
    
    let itemsData: [PieChart.ItemData]
    private var sliceViewsData: Binding<[PieChart.SliceViewData]> {
        Binding(
            get: {
                var newSliceViewsData: [PieChart.SliceViewData] = []
                
                let totalValue = itemsData.map { itemData in
                    itemData.value
                }
                    .reduce(0, +)
                
                var lastDegree: Double = 0.0
                let degreesForSlices = 360.0 - (separatorDegrees * Double(itemsData.count))
                let separatorDegreesHalf = separatorDegrees / 2.0
                
                for itemData in itemsData {
                    
                    let degrees = degreesForSlices * Double(itemData.value) / Double(totalValue)
                    let degreesFrom = lastDegree + separatorDegreesHalf
                    let degreesTo = degreesFrom + degrees
                    lastDegree = degreesTo + separatorDegreesHalf
                    
                    newSliceViewsData.append(
                        PieChart.SliceViewData(
                            id: itemData.id,
                            degreesFrom: degreesFrom,
                            degreesTo: degreesTo,
                            itemData: itemData
                        )
                    )
                }
                
                return newSliceViewsData
            },
            set: { _ in
                fatalError() // todo
            }
        )
    }
    
    var body: some View {
        ZStack {
            ForEach(sliceViewsData, id: \.id) { sliceViewData in
                SliceView(
                    selectedId: selectedId,
                    onIdSelected: { onIdSelected($0) },
                    data: sliceViewData,
                    separatorDegrees: separatorDegrees,
                    ringRatio: ringRatio
                )
            }
            
            if let selectedItem = itemsData.first(where: { $0.id == selectedId }) {
                GeometryReader { geometry in
                    VStack(spacing: 6) {
                        
                        if let subtitleTop = selectedItem.subtitleTop {
                            Text(subtitleTop)
                                .multilineTextAlignment(.center)
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                                .foregroundColor(Color(UIColor.secondaryLabel))
                                .font(.system(size: 14.5, weight: .light))
                        }
                        
                        Text(selectedItem.title)
                            .multilineTextAlignment(.center)
                            .lineLimit(2)
                            .lineSpacing(-6)
                            .minimumScaleFactor(0.8)
                            .frame(width: (geometry.size.width - 120.0) / ringRatio)
                        
                        if let subtitleBottom = selectedItem.subtitleBottom {
                            Text(subtitleBottom)
                                .multilineTextAlignment(.center)
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                                .foregroundColor(Color(UIColor.secondaryLabel))
                                .font(.system(size: 14.5, weight: .light))
                        }
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        }
    }
    
    
    //
    // Slice view
    
    struct SliceView: View {
        
        let selectedId: String?
        let onIdSelected: (String?) -> Void
        
        @Binding var data: PieChart.SliceViewData
        
        let separatorDegrees: Double
        let ringRatio: Double
        let selectedOffset = 25.0
        
        var body: some View {
            
            let middleDegrees = (data.degreesTo + data.degreesFrom) / 2.0
            let selectedOffsetX = sinDegrees(middleDegrees) * selectedOffset
            let selectedOffsetY = cosDegrees(middleDegrees) * selectedOffset
            
            GeometryReader { geometry in
                
                let size: CGFloat = min(geometry.size.width, geometry.size.height)
                let center = CGPoint(x: size * 0.5, y: size * 0.5)
                
                let maxBorder: Double = 16.0
                
                let oRadius = (size / 2) - (maxBorder / 2) // outer radius
                let iRadius = oRadius / ringRatio // inner radius
                
                let oc = 2.0 * Double.pi * oRadius
                let ic = 2.0 * Double.pi * iRadius
                
                let isa: CGFloat = (data.degreesTo - data.degreesFrom) * ic / 360.0 // inner segment arc
                let isMaxBorder = isa > maxBorder
                let border: Double = isMaxBorder ? maxBorder : isa
                
                let extraRadius: Double = isMaxBorder ? 0.0 : (maxBorder / 2.0) - (border / 2.0)
                
                let epsSafe = 0.00001
                let oPd: Double = ((border / 2) * 360) / oc - epsSafe // Outer padding
                let iPd: Double = ((border / 2) * 360) / ic - epsSafe // Inner padding
                
                let extraOPd = separatorDegrees / 2.0 * (1.0 - (iRadius / oRadius))
                
                let oAStart = Angle(degrees: -90.0 + data.degreesFrom + oPd - extraOPd)
                let oAEnd = Angle(degrees: -90.0 + data.degreesTo - oPd + extraOPd)
                let iAStart = Angle(degrees: -90.0 + data.degreesFrom + iPd)
                let iAEnd = Angle(degrees: -90.0 + data.degreesTo - iPd)
                
                // Border
                createPath(
                    center: center,
                    oRadius: oRadius + extraRadius,
                    iRadius: iRadius - extraRadius,
                    oAStart: oAStart,
                    oAEnd: oAEnd,
                    iAStart: iAStart,
                    iAEnd: iAEnd,
                    withFinishedArc: true
                )
                .stroke(
                    data.itemData.color.toColor(),
                    style: StrokeStyle(
                        lineWidth: border,
                        lineCap: .round,
                        lineJoin: .round
                    )
                )
                
                // Fill
                createPath(
                    center: center,
                    oRadius: oRadius + extraRadius,
                    iRadius: iRadius - extraRadius,
                    oAStart: oAStart,
                    oAEnd: oAEnd,
                    iAStart: iAStart,
                    iAEnd: iAEnd,
                    withFinishedArc: false
                )
                .fill(data.itemData.color.toColor())
                
                let isShortTitle = isa <= 20
                let hintRadius = (oRadius + iRadius) / 2 * (isShortTitle ? 1.08 : 1.02)
                let hintWidth = 65.0
                let hintHeight = 30.0
                let hintX = sinDegrees(middleDegrees) * hintRadius + center.x - hintWidth / 2
                let hintY = cosDegrees(middleDegrees) * hintRadius - center.y + hintHeight / 2
                let title = isShortTitle ? data.itemData.shortTitle : data.itemData.title
                Text(title)
                    .offset(x: hintX, y: -hintY)
                    .lineSpacing(-6)
                    .lineLimit(2)
                    .minimumScaleFactor(0.8)
                    .frame(width: hintWidth, height: hintHeight, alignment: .center)
                    .multilineTextAlignment(.center)
                    .font(.system(size: 14.5))
                    .foregroundColor(Color.white)
            }
            .aspectRatio(1, contentMode: .fit)
            .padding(selectedOffset)
            .offset(
                x: selectedId == data.id ? selectedOffsetX : 0,
                y: selectedId == data.id ? -selectedOffsetY : 0
            )
            .onTapGesture {
                withAnimation {
                    onIdSelected(selectedId == data.id ? nil : data.id)
                }
            }
        }
        
        private func createPath(
            center: CGPoint,
            oRadius: Double,
            iRadius: Double,
            oAStart: Angle,
            oAEnd: Angle,
            iAStart: Angle,
            iAEnd: Angle,
            withFinishedArc: Bool
        ) -> Path {
            
            Path { path in
                
                // Linked to the 3rd arc
                path.addArc(center: center, radius: oRadius, startAngle: oAStart, endAngle: oAEnd, clockwise: false)
                
                path.addArc(center: center, radius: iRadius, startAngle: iAEnd, endAngle: iAStart, clockwise: true)
                
                if withFinishedArc {
                    // Duplicates the 1st arc for a smoothed angle effect
                    path.addArc(center: center, radius: oRadius, startAngle: oAStart, endAngle: oAEnd, clockwise: false)
                }
            }
        }
    }
}
