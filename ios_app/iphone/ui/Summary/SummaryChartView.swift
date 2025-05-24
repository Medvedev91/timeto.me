import SwiftUI
import shared

struct SummaryChartView: View {
    
    let activitiesUi: [SummaryVm.ActivityUi]
    
    var body: some View {
        VmView({
            SummaryChartVm(
                activitiesUi: activitiesUi
            )
        }) { _, state in
            SummaryChartViewInner(
                state: state
            )
        }
    }
}

private struct SummaryChartViewInner: View {
    
    let state: SummaryChartVm.State
    
    ///
    
    @State private var selectedId: String? = nil
    
    var body: some View {
        
        GeometryReader { geometry in
            
            VStack {
                
                let pieWidth: Double = min(geometry.size.width - 30, geometry.size.height / 1.85)
                SummaryPieView(
                    selectedId: selectedId,
                    onIdSelected: { selectedId = $0 },
                    itemsData: state.pieItems
                )
                .frame(width: pieWidth, height: pieWidth)
                
                //
                // Grid list
                
                let cellHeight = 40.0
                let padding = 20.0
                let itemWidth: Double = geometry.size.width / 2.0
                
                ScrollView {
                    
                    let columnsCount: Int = Int(ceil(Double(state.pieItems.count) / 2.0))
                    let columns: [GridItem] = Array(repeating: .init(.fixed(cellHeight)), count: columnsCount)
                    LazyHGrid(rows: columns, spacing: 0) {
                        ForEach(state.pieItems, id: \.id) { pie in
                            ChartView_ItemCellView(
                                itemData: pie,
                                selectedId: selectedId,
                                onIdSelected: { selectedId = $0 },
                                width: itemWidth - padding,
                                height: cellHeight
                            )
                        }
                    }
                    .padding(.leading, padding)
                    .padding(.trailing, padding)
                }
            }
            .frame(maxWidth: .infinity) // Иначе, если всего один элемент не на всю ширину
            .padding(.top, 8)
        }
        .background(.background)
    }
}

private struct ChartView_ItemCellView: View {
    
    let itemData: PieChart.ItemData
    
    let selectedId: String?
    let onIdSelected: (String?) -> Void
    
    let width: Double
    let height: Double
    
    var body: some View {
        Button(
            action: {
                withAnimation {
                    onIdSelected(selectedId == itemData.id ? nil : itemData.id)
                }
            },
            label: {
                
                HStack(spacing: 10) {
                    
                    let lineHeight = 10.0
                    
                    RoundedRectangle(cornerRadius: lineHeight / 2, style: .continuous)
                        .fill(itemData.color.toColor())
                        .frame(
                            maxWidth: selectedId == itemData.id ? lineHeight * 3 : lineHeight,
                            maxHeight: .infinity
                        )
                        .padding(.leading, 6)
                    
                    VStack(alignment: .leading, spacing: 0) {
                        
                        Text(itemData.title)
                            .lineLimit(1)
                            .foregroundColor(Color(.label))
                        
                        HStack {
                            
                            Text(itemData.customData as! String)
                                .foregroundColor(Color(.secondaryLabel))
                                .font(.system(size: 14, weight: .light))
                            
                            Spacer()
                            
                            Text(itemData.subtitleTop!)
                                .foregroundColor(Color(.secondaryLabel))
                                .font(.system(size: 14, weight: .light))
                        }
                        .padding(.top, 2)
                    }
                    
                    Spacer()
                    
                }
                .frame(width: width, height: height)
            }
        )
    }
}
