import SwiftUI
import shared

private let pTextLineHeight = 4.0

struct ReadmeSheet: View {

    @Binding var isPresented: Bool

    @State private var vm = ReadmeSheetVM()
    @State private var scroll = 0

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                title: state.title,
                scrollToHeader: scroll,
                bgColor: c.sheetBg
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {

                VStack {

                    ForEachIndexed(state.paragraphs) { _, paragraph in

                        if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTitle {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 28, weight: .bold))
                                    .padding(.top, 44)
                                    .padding(.horizontal, H_PADDING)
                                Spacer()
                            }
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphText {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 18))
                                    .padding(.top, 24)
                                    .padding(.horizontal, H_PADDING)
                                    .lineSpacing(pTextLineHeight)
                                Spacer()
                            }
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTextHighlight {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 18))
                                    .padding(.top, 16)
                                    .padding(.bottom, 16)
                                    .padding(.horizontal, H_PADDING)
                                    .lineSpacing(pTextLineHeight)
                                Spacer()
                            }
                            .background(c.blue)
                            .padding(.top, 24)
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerTypical {
                            ImagePreviewsView(
                                images: [
                                    "readme_timer_1"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerMyActivities {
                            ImagePreviewsView(
                                images: [
                                    "readme_activities_1"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerCharts {
                            ImagePreviewsView(
                                images: [
                                    "readme_chart_1",
                                    "readme_chart_2",
                                    "readme_chart_3"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerPractice1 {
                            ImagePreviewsView(
                                images: [
                                    "readme_timer_practice_1",
                                    "readme_timer_practice_2",
                                    "readme_timer_practice_3",
                                    "readme_timer_practice_4"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerPractice2 {
                            ImagePreviewsView(
                                images: [
                                    "readme_timer_practice_5",
                                    "readme_chart_2",
                                    "readme_chart_3"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsMy {
                            ImagePreviewsView(
                                images: [
                                    "readme_repeatings_1"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsToday {
                            ImagePreviewsView(
                                images: [
                                    "readme_repeatings_2"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsPractice1 {
                            ImagePreviewsView(
                                images: [
                                    "readme_repeating_practice_1",
                                    "readme_repeating_practice_2",
                                    "readme_repeating_practice_3"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsPractice2 {
                            ImagePreviewsView(
                                images: [
                                    "readme_repeating_practice_4",
                                    "readme_repeating_practice_5"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsExamples {
                            ImagePreviewsView(
                                images: [
                                    "readme_checklists_1",
                                    "readme_checklists_2",
                                    "readme_checklists_3"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsPractice1 {
                            ImagePreviewsView(
                                images: [
                                    "readme_checklists_practice_1",
                                    "readme_checklists_practice_2",
                                    "readme_checklists_practice_3",
                                    "readme_checklists_practice_4",
                                    "readme_checklists_practice_5",
                                    "readme_checklists_practice_6",
                                    "readme_checklists_practice_7"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsPractice2 {
                            ImagePreviewsView(
                                images: [
                                    "readme_checklists_practice_8",
                                    "readme_checklists_practice_9"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphGoalsExamples {
                            ImagePreviewsView(
                                images: [
                                    "readme_goals_1"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphCalendarExamples {
                            ImagePreviewsView(
                                images: [
                                    "readme_calendar_1",
                                    "readme_calendar_2"
                                ]
                            )
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphAskAQuestion {
                            AskAQuestionButtonView(
                                subject: paragraph.subject,
                                isFirst: true,
                                isLast: true,
                                withTopDivider: false
                            )
                                .padding(.top, 36)
                        } else {
                            fatalError()
                        }
                    }

                    Spacer()
                        .padding(.top, 36)
                }
            }

            Sheet__BottomViewClose {
                isPresented = false
            }
        }
    }
}

private struct ImagePreviewsView: View {

    let images: [String]

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        ScrollView(.horizontal, showsIndicators: false) {

            HStack {

                Padding(horizontal: 10)

                ForEachIndexed(images) { _, item in

                    Button(
                        action: {
                            nativeSheet.show { isSliderPresented in
                                ImagesSlider(
                                    isPresented: isSliderPresented,
                                    images: images
                                )
                            }
                        },
                        label: {
                            Image(item)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .cornerRadius(16)
                                .shadow(radius: 4)
                                .frame(height: 350)
                                .padding(.horizontal, 6)
                                // Paddings for shadow radius
                                .padding(.top, 28)
                                .padding(.bottom, 4)
                        }
                    )
                }

                Padding(horizontal: 10)
            }
        }
    }
}

private struct ImagesSlider: View {

    @Binding var isPresented: Bool
    let images: [String]

    var body: some View {

        VStack {

            ScrollView(.horizontal, showsIndicators: false) {

                HStack {

                    Padding(horizontal: 10)

                    ForEachIndexed(images) { _, item in

                        Image(item)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .cornerRadius(16)
                            .shadow(radius: 8)
                            .frame(height: .infinity)
                            .padding(.horizontal, 12)
                            // Paddings for shadow radius
                            .padding(.top, 16)
                            .padding(.bottom, 12)
                    }

                    Padding(horizontal: 10)
                }
            }

            HStack {
                Spacer()
                Button(
                    action: {
                        isPresented = false
                    },
                    label: {
                        Text("close")
                            .foregroundColor(c.text)
                    }
                )
                .padding(.top, 12)
                .padding(.bottom, 12)
                .padding(.trailing, 36)
            }
        }
    }
}
